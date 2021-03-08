package ru.bulldog.justchat;

import ru.bulldog.justchat.network.ChatServer;

import java.io.IOException;
import java.util.Scanner;

public class JustChatServer {

	private final static Scanner CONSOLE_INPUT = new Scanner(System.in);
	public final static Logger LOGGER = new Logger(JustChatServer.class);

	public static void main(String[] args) {
		try (ChatServer server = new ChatServer()) {
			server.launch();
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
	}
}
