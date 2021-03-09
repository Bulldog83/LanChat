package ru.bulldog.justchat.server.network;

import java.util.HashMap;
import java.util.Map;

public class AuthInfo {

	private final static Map<String, String> users = new HashMap<>();

	public static boolean checkAuthData(String login, String password) {
		if (!users.containsKey(login)) return false;
		return users.get(login).equals(password);
	}

	static {
		users.put("Qwe", "qwerty");
		users.put("Wer", "12345");
		users.put("Ewq", "54321");
		users.put("Ert", "password");
	}
}
