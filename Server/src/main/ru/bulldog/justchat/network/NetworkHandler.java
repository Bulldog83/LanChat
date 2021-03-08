package ru.bulldog.justchat.network;

import ru.bulldog.justchat.AuthInfo;
import ru.bulldog.justchat.Logger;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class NetworkHandler implements Closeable {

	private final static Logger LOGGER = new Logger(NetworkHandler.class);

	private final Map<String, ChatClient> clients;

	public NetworkHandler() {
		this.clients = new HashMap<>();
	}

	public void onClientJoin(ChatClient client) {
		new Thread(() -> {
			try {
				while (!client.socket.isClosed()) {
					String loginInfo = client.input.readUTF();
					if (loginInfo.startsWith("/login")) {
						String[] loginData = loginInfo.substring(7).split(":");
						if (loginData.length < 3) {
							client.sendMessage("Server: Wrong auth data.");
						} else {
							if (AuthInfo.checkLogin(loginData[0], loginData[1])) {
								client.networkHandler = this;
								client.nickName = loginData[2];
								clients.put(loginData[2], client);
								client.listen();
								System.out.println(loginData[2] + " successfully authorized.");
								break;
							} else {
								client.sendMessage("Server: Invalid login or password.");
								LOGGER.warn("Login failed: " + loginData[0] + " - " + loginData[1]);
							}
						}
					}
					client.sendMessage("Server: Wrong auth data.");
				}
			} catch (IOException ex) {
				LOGGER.error("Error client logging in", ex);
			}
		});
	}

	public void onClientLeave(ChatClient client) {
		clients.remove(client.nickName);
	}

	public void sendMessage(String message) {
		clients.values().forEach(client -> {
			if (!client.socket.isClosed()) {
				client.sendMessage(message);
			}
		});
	}

	public void sendMessage(ChatClient sender, String nickName, String message) {
		if (clients.containsKey(nickName)) {
			clients.get(nickName).sendMessage(message);
		} else {
			sender.sendMessage("Server: No clients with nickname '" + nickName + "' found.");
		}
	}

	@Override
	public void close() throws IOException {
		for (ChatClient client : clients.values()) {
			client.sendMessage("Server closed.");
			client.close();
		}
	}

	public static class ChatClient implements Closeable {
		private final Socket socket;
		private final DataInputStream input;
		private final DataOutputStream output;

		private NetworkHandler networkHandler;
		private String nickName;

		private boolean listening = true;

		public ChatClient(Socket socket) throws IOException {
			this.socket = socket;
			this.input = new DataInputStream(socket.getInputStream());
			this.output = new DataOutputStream(socket.getOutputStream());
		}

		private void listen() {
			new Thread(() -> {
				try {
					while (listening) {
						String inputMessage = input.readUTF();
						if (!inputMessage.trim().equals("")) {
							if (inputMessage.equals("/quit")) {
								networkHandler.onClientLeave(this);
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
					LOGGER.error("Error read message from " + nickName, ex);
				}
			});
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
