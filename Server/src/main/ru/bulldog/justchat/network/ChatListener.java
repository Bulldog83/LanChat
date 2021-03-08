package ru.bulldog.justchat.network;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;

public class ChatListener implements Closeable {

	public static void initialize() {
		try {
			ChatListener chatListener = new ChatListener();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private final NetworkHandler networkHandler;
	private final ServerSocket server;

	private ChatListener() throws IOException {
		this.networkHandler = new NetworkHandler();
		this.server = new ServerSocket(10000);
	}

	@Override
	public void close() throws IOException {

	}
}
