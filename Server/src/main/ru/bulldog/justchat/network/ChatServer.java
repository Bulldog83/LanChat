package ru.bulldog.justchat.network;

import ru.bulldog.justchat.Logger;
import ru.bulldog.justchat.network.NetworkHandler.ChatClient;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatServer implements Closeable {

	public final static Logger LOGGER = new Logger(ChatServer.class);
	public final static int PORT = 10000;

	private final NetworkHandler networkHandler;
	private final ServerSocket server;

	private boolean listening = true;

	public ChatServer() throws IOException {
		this.networkHandler = new NetworkHandler();
		this.server = new ServerSocket(PORT);
	}

	public void launch() {
		new Thread(() -> {
			while (listening) {
				try {
					Socket clientSocket = server.accept();
					ChatClient client = new ChatClient(clientSocket);
					networkHandler.onClientJoin(client);
				} catch (IOException ex) {
					LOGGER.error("Client connection error", ex);
				}
			}
		});
		LOGGER.info("Server started.");
	}

	@Override
	public void close() throws IOException {
		listening = false;
		networkHandler.close();
		server.close();
	}
}
