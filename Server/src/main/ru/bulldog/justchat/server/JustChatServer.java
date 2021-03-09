package ru.bulldog.justchat.server;

import ru.bulldog.justchat.Logger;
import ru.bulldog.justchat.server.network.ChatServer;

import java.io.IOException;
import java.util.Scanner;

public class JustChatServer {

	private final static Scanner CONSOLE_INPUT = new Scanner(System.in);
	public final static Logger LOGGER = new Logger(JustChatServer.class);

	public static void main(String[] args) {
		try (ChatServer server = new ChatServer()) {
			new Thread(() -> {
				try {
					while (true) {
						if (CONSOLE_INPUT.hasNext()) {
							String command = CONSOLE_INPUT.nextLine();
							if (command.equals("/stop")) {
								server.close();
								break;
							}
						}
					}
				} catch (IOException ex) {
					LOGGER.error("Chat server error", ex);
				}
			}, "Console Input").start();
			server.launch();
		} catch (IOException ex) {
			LOGGER.error("Chat server error", ex);
		}
	}
}
