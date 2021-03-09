package ru.bulldog.justchat.client.network;

import ru.bulldog.justchat.Logger;
import ru.bulldog.justchat.server.network.ChatServer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientNetworkHandler {

	private final static Logger LOGGER = new Logger(ClientNetworkHandler.class);

	private Socket connection;
	private DataInputStream dataInput;
	private DataOutputStream dataOutput;

	private final MessageListener messageListener;

	private boolean listening = true;

	public ClientNetworkHandler(MessageListener messageListener) {
		this.messageListener = messageListener;
	}

	public boolean joinServer(String address, String login, String password, String nickName) {
		try {
			connection = new Socket(address, ChatServer.PORT);
			if (connection.isConnected()) {
				dataInput = new DataInputStream(connection.getInputStream());
				dataOutput = new DataOutputStream(connection.getOutputStream());
				String loginData = String.format("/login %s:%s:%s", login, password, nickName);
				dataOutput.writeUTF(loginData);
				String requestData = dataInput.readUTF();
				if (requestData.equals("/success")) {
					messageListener.onMessageReceived("You successfully join server.");
					onJoinServer();
					return true;
				}
				if (requestData.startsWith("/fail")) {
					String requestMessage = requestData.substring(6);
					messageListener.onMessageReceived("Server: " + requestMessage);
				} else {
					messageListener.onMessageReceived("Wrong server request data.");
				}
			}
			return false;
		} catch (IOException ex) {
			LOGGER.error("Connection error", ex);
		}
		return false;
	}

	public void onJoinServer() {
		listening = true;
		new Thread(() -> {
			try {
				while (listening) {
					String message = dataInput.readUTF();
					if (messageListener != null) {
						messageListener.onMessageReceived(message);
					}
				}
			} catch (IOException ex) {
				if (listening) {
					LOGGER.error("Message receiving error", ex);
					listening = false;
				}
			}
		}, "Server Message Listener").start();
	}

	public void onLeaveServer() {
		if (listening) {
			sendMessage("/quit");
			try {
				connection.close();
			} catch (IOException ex) {
				LOGGER.error("Connection close error", ex);
			} finally {
				messageListener.onMessageReceived("You left the server.");
				listening = false;
			}
		}
	}

	public boolean sendMessage(String message) {
		try {
			if (!message.trim().equals("")) {
				dataOutput.writeUTF(message);
				return true;
			}
		} catch (IOException ex) {
			LOGGER.error("Send message error", ex);
		}
		return false;
	}
}
