package ru.bulldog.justchat.server.storage;

import java.util.HashMap;
import java.util.Map;

public class BasicAuthService implements AuthService {

	private final Map<String, UserData> users = new HashMap<>();

	@Override
	public String getNickname(String login, String password) {
		login = login.toLowerCase();
		if (!users.containsKey(login)) return null;
		if (users.get(login).getPassword().equals(password)) {
			return users.get(login).getNickname();
		}
		return null;
	}

	@Override
	public boolean registerUser(String login, String password, String nickname) {
		for (UserData user : users.values()) {
			if (user.getLogin().equals(login) || user.getNickname().equals(nickname)) {
				return false;
			}
		}
		users.put(login, new UserData(login, password, nickname));
		return true;
	}

	@Override
	public boolean deleteUser(String login) {
		return users.remove(login) != null;
	}

	public BasicAuthService() {
		users.put("qwe", new UserData("qwe", "qwerty", "Qwe"));
		users.put("wer", new UserData("wer", "12345", "Wer"));
		users.put("ewq", new UserData("ewq", "54321", "Ewq"));
		users.put("ert", new UserData("ert", "password", "Ert"));
	}
}
