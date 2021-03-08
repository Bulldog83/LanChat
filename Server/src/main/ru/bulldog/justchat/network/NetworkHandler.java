package ru.bulldog.justchat.network;

import java.util.HashMap;
import java.util.Map;

public class NetworkHandler {

	private final Map<String, ChatClient> clients;

	public NetworkHandler() {
		this.clients = new HashMap<>();
	}

	public static class ChatClient {

	}
}
