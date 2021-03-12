package ru.bulldog.justchat.server.network;

import ru.bulldog.justchat.Logger;
import ru.bulldog.justchat.server.network.ServerNetworkHandler.ClientHandler;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatServer implements Closeable {

	public final static int PORT = 10000;

	private final static Logger LOGGER = new Logger(ChatServer.class);

	private final ServerNetworkHandler networkHandler;
	private final ServerSocket server;

	private boolean closed = false;

	public ChatServer() throws IOException {
		this.networkHandler = new ServerNetworkHandler();
		this.server = new ServerSocket(PORT);
	}

	public void launch() {
		LOGGER.info("Server started.");
		while (!closed) {
			try {
				Socket clientSocket = server.accept();
				ClientHandler client = new ClientHandler(clientSocket);
				networkHandler.onClientJoin(client);
			} catch (IOException ex) {
				if (!closed) {
					LOGGER.error("Client connection error", ex);
				}
			}
		}
		LOGGER.info("Server stopped.");
	}

	@Override
	public void close() throws IOException {
		if (!closed) {
			LOGGER.info("Server stopping...");
			closed = true;
			networkHandler.close();
			server.close();
		}
	}
}
