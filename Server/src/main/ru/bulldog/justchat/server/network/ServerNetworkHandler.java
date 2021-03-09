package ru.bulldog.justchat.server.network;

import ru.bulldog.justchat.Logger;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ServerNetworkHandler implements Closeable {

	private final static Logger LOGGER = new Logger(ServerNetworkHandler.class);

	private final Map<String, ChatClient> clients;

	private boolean closed = false;

	public ServerNetworkHandler() {
		this.clients = new HashMap<>();
	}

	public void onClientJoin(ChatClient client) {
		new Thread(() -> {
			try {
				while (!closed) {
					String loginInfo = client.input.readUTF();
					if (loginInfo.startsWith("/login")) {
						String[] loginData = loginInfo.substring(7).split(":");
						if (loginData.length < 3) {
							client.sendMessage("Server: Wrong auth data.");
						} else {
							String nickName = loginData[2];
							if (clients.containsKey(nickName)) {
								client.sendMessage("/fail Nickname " + nickName + " already registered.");
							} else if (AuthInfo.checkAuthData(loginData[0], loginData[1])) {
								sendMessage(nickName + " join.");
								client.output.writeUTF("/success");
								client.networkHandler = this;
								client.nickName = nickName;
								client.listen();
								clients.put(nickName, client);
								LOGGER.info(nickName + " successfully authorized.");
								break;
							} else {
								client.sendMessage("/fail Invalid login or password.");
								LOGGER.warn("Login failed: " + loginData[0] + " - " + loginData[1]);
							}
						}
					}
					client.sendMessage("Server: Wrong auth data.");
				}
			} catch (IOException ex) {
				if (!closed) {
					LOGGER.error("Error client logging in", ex);
				}
			}
		}, "Client Login Listener").start();
	}

	public void onClientLeave(ChatClient client) {
		clients.remove(client.nickName);
		sendMessage(client.nickName + " leave.");
	}

	public void sendMessage(String message) {
		clients.values().forEach(client -> {
			if (client.listening) {
				client.sendMessage(message);
			}
		});
	}

	public void sendMessage(ChatClient sender, String nickName, String message) {
		if (clients.containsKey(nickName)) {
			clients.get(nickName).sendMessage(message);
			clients.get(sender.nickName).sendMessage(message);
		} else {
			sender.sendMessage("Server: No clients with nickname '" + nickName + "' found.");
		}
	}

	@Override
	public void close() throws IOException {
		if (!closed) {
			closed = true;
			for (ChatClient client : clients.values()) {
				if (client.listening) {
					client.sendMessage("Server closed.");
					client.close();
				}
			}
		}
	}

	public static class ChatClient implements Closeable {
		private final Socket socket;
		private final DataInputStream input;
		private final DataOutputStream output;

		private ServerNetworkHandler networkHandler;
		private String nickName;

		private boolean listening = true;

		public ChatClient(Socket socket) throws IOException {
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
									String message = nickName + ": " + inputMessage.substring(idx + 1);
									networkHandler.sendMessage(this, targetNickName, message);
								} else {
									networkHandler.sendMessage(nickName + ": " + inputMessage);
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
