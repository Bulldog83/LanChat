package ru.bulldog.justchat.client.network;

import ru.bulldog.justchat.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;

public class ClientNetworkHandler {

	private final static Logger LOGGER = Logger.getLogger(ClientNetworkHandler.class);

	private final SimpleDateFormat dateFormat;
	private Socket connection;
	private DataInputStream dataInput;
	private DataOutputStream dataOutput;

	private final MessageListener messageListener;

	private boolean listening = false;

	public ClientNetworkHandler(MessageListener messageListener) {
		this.dateFormat = new SimpleDateFormat("HH:mm:ss ");
		this.messageListener = messageListener;
	}

	private void createConnection(String address) throws IOException {
		if (!isConnected()) {
			connection = new Socket(address, 10000);
			dataInput = new DataInputStream(connection.getInputStream());
			dataOutput = new DataOutputStream(connection.getOutputStream());
			connection.setSoTimeout(60000);
		}
	}

	private boolean processRequest(MessageListener statusListener) throws IOException {
		String requestData = dataInput.readUTF();
		if (requestData.startsWith("/success")) {
			connection.setSoTimeout(0);
			String nickName = requestData.substring(9);
			messageListener.onJoinServer(nickName);
			messageListener.onMessageReceived("You successfully join server.");
			onJoinServer();
			return true;
		}
		if (requestData.startsWith("/fail")) {
			String requestMessage = requestData.substring(6);
			statusListener.onMessageReceived("Server: " + requestMessage);
		} else {
			statusListener.onMessageReceived("Wrong server request data.");
			LOGGER.info("Server request: " + requestData);
		}
		return false;
	}

	public boolean joinServer(String address, String login, String password) {
		try {
			createConnection(address);
			if (isConnected()) {
				String loginData = String.format("/login %s:%s", login, password);
				dataOutput.writeUTF(loginData);
				return processRequest(messageListener);
			}
		} catch (IOException ex) {
			LOGGER.error("Connection error", ex);
		}
		return false;
	}

	public boolean doRegistration(MessageListener statusListener, String address, String login, String password, String nickName) {
		try {
			createConnection(address);
			if (isConnected()) {
				String registerData = String.format("/register %s:%s:%s", login, password, nickName);
				dataOutput.writeUTF(registerData);
				return processRequest(statusListener);
			}
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
					if (message.startsWith("/")) {
						processSystemMsg(message);
					} else if (messageListener != null) {
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

	private void processSystemMsg(String message) {
		if (message.startsWith("/private")) {
			messageListener.onMessageReceived("[*]" + message.substring(9));
		}
		if (message.startsWith("/client") || message.startsWith("/users")) {
			messageListener.onMessageReceived(message);
		}
	}

	public void onLeaveServer() {
		if (isConnected() && listening) {
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

	public boolean isConnected() {
		if (connection != null) {
			System.out.println("Closed: " + connection.isClosed());
			System.out.println("Connected: " + connection.isConnected());
		}
		return connection != null && connection.isConnected();
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

	public SimpleDateFormat getDateFormat() {
		return dateFormat;
	}
}
