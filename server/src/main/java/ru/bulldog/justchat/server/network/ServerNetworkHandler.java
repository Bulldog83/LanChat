package ru.bulldog.justchat.server.network;

import ru.bulldog.justchat.Logger;
import ru.bulldog.justchat.server.storage.AuthService;
import ru.bulldog.justchat.server.storage.DBAuthService;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

public class ServerNetworkHandler implements Closeable {

	private final static Logger LOGGER = new Logger(ServerNetworkHandler.class);
	private final static int TIMEOUT = 60000;

	private final Map<String, ClientHandler> clients;
	private final AuthService authService;

	private boolean closed = false;

	public ServerNetworkHandler() {
		this.clients = new HashMap<>();
		this.authService = new DBAuthService();
	}

	public void onClientJoin(ClientHandler client) {
		new Thread(() -> {
			try {
				LOGGER.info("Client connected with address: " + client.socket.getInetAddress());
				client.socket.setSoTimeout(TIMEOUT);
				while (!closed) {
					String loginInfo = client.input.readUTF();
					if (loginInfo.startsWith("/register")) {
						String[] registerData = loginInfo.substring(10).split(":");
						if (registerData.length < 3) {
							client.sendMessage("/fail Wrong registration data.");
						} else if (authService.registerUser(registerData[0], registerData[1], registerData[2])) {
							LOGGER.info("Registered new user: " + registerData[2]);
							client.socket.setSoTimeout(0);
							registerClient(client, registerData[2]);
							break;
						} else {
							client.sendMessage("/fail Login or Nickname already used.");
						}
					} else if (loginInfo.startsWith("/login")) {
						String[] loginData = loginInfo.substring(7).split(":");
						if (loginData.length < 2) {
							client.sendMessage("Server: Wrong auth data.");
						} else {
							String nickName = authService.getNickname(loginData[0], loginData[1]);
							if (nickName != null) {
								if (clients.containsKey(nickName)) {
									client.sendMessage("/fail User " + nickName + " already logged in.");
								} else {
									client.socket.setSoTimeout(0);
									registerClient(client, nickName);
									break;
								}
							} else {
								client.sendMessage("/fail Invalid login or password or user not registered.");
								LOGGER.warn("Login failed: " + loginData[0] + " - " + loginData[1]);
							}
						}
					}
					client.sendMessage("/fail Wrong auth data.");
					LOGGER.info("Client data: " + loginInfo);
				}
			} catch (SocketTimeoutException ignored) {
				try {
					LOGGER.info("Client disconnected by timeout: " + client.socket.getInetAddress());
					client.socket.close();
				} catch (IOException ex) {
					LOGGER.error("Client disconnect error", ex);
				}
			} catch (IOException ex) {
				if (!closed) {
					LOGGER.error("Error client logging in", ex);
				}
			}
		}, "Client Login Listener").start();
	}

	private void registerClient(ClientHandler client, String nickName) throws IOException {
		client.socket.setSoTimeout(0);
		client.sendMessage("/success " + nickName);
		client.networkHandler = this;
		client.nickName = nickName;
		client.listen();
		clients.put(nickName, client);
		sendMessage("/clientjoin " + nickName);
		sendMessage("/users " + String.join(":", clients.keySet()));
		LOGGER.info(nickName + " successfully authorized.");
	}

	public void onClientLeave(ClientHandler client) {
		clients.remove(client.nickName);
		sendMessage("/clientleave " + client.nickName);
	}

	public void sendMessage(String message) {
		clients.values().forEach(client -> {
			if (client.listening) {
				client.sendMessage(message);
			}
		});
	}

	public void sendMessage(ClientHandler sender, String nickName, String message) {
		if (clients.containsKey(nickName)) {
			message = "/private " + message;
			clients.get(nickName).sendMessage(message);
			if (!sender.nickName.equals(nickName)) {
				sender.sendMessage(message);
			}
		} else {
			sender.sendMessage("Server: No clients with nickname '" + nickName + "' found.");
		}
	}

	@Override
	public void close() throws IOException {
		if (!closed) {
			closed = true;
			for (ClientHandler client : clients.values()) {
				if (client.listening) {
					client.sendMessage("Server closed.");
					client.close();
				}
			}
		}
	}

	public static class ClientHandler implements Closeable {
		private final Socket socket;
		private final DataInputStream input;
		private final DataOutputStream output;

		private ServerNetworkHandler networkHandler;
		private String nickName;

		private boolean listening = true;

		public ClientHandler(Socket socket) throws IOException {
			this.socket = socket;
			this.input = new DataInputStream(socket.getInputStream());
			this.output = new DataOutputStream(socket.getOutputStream());
		}

		private void listen() {
			listening = true;
			new Thread(() -> {
				try {
					while (listening) {
						String inputMessage = input.readUTF();
						if (!inputMessage.trim().equals("")) {
							if (inputMessage.equals("/quit")) {
								networkHandler.onClientLeave(this);
								LOGGER.info("Client disconnected: " + nickName);
								close();
								break;
							} else {
								if (inputMessage.startsWith("/msg")) {
									int idx = inputMessage.indexOf(' ', 5);
									if (idx == -1) continue;
									String targetNickName = inputMessage.substring(5, idx);
									String message = "[" + nickName + "]: " + inputMessage.substring(idx + 1);
									networkHandler.sendMessage(this, targetNickName, message);
								} else {
									networkHandler.sendMessage("[" + nickName + "]: " + inputMessage);
								}
							}
						}
					}
				} catch (IOException ex) {
					if (listening) {
						LOGGER.error("Error read message from " + nickName, ex);
						listening = false;
					}
				}
			}, "Client Message Listener").start();
		}

		public void sendMessage(String message) {
			try {
				output.writeUTF(message);
			} catch (IOException ex) {
				LOGGER.error("Error send message to " + nickName, ex);
			}
		}

		@Override
		public void close() throws IOException {
			listening = false;
			socket.close();
		}
	}
}
