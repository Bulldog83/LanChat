package ru.bulldog.justchat.server.storage;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class BasicAuthService implements AuthService {

	private final Map<String, UserData> users = new HashMap<>();

	@Nullable
	public String getNickname(String login, String password) {
		login = login.toLowerCase();
		if (!users.containsKey(login)) return null;
		if (users.get(login).password.equals(password)) {
			return users.get(login).nickname;
		}
		return null;
	}

	@Override
	public boolean registerUser(String login, String password, String nickname) {
		for (UserData user : users.values()) {
			if (user.login.equals(login) || user.nickname.equals(nickname)) {
				return false;
			}
		}
		users.put(login, new UserData(login, password, nickname));
		return true;
	}

	public BasicAuthService() {
		users.put("qwe", new UserData("qwe", "qwerty", "Qwe"));
		users.put("wer", new UserData("wer", "12345", "Wer"));
		users.put("ewq", new UserData("ewq", "54321", "Ewq"));
		users.put("ert", new UserData("ert", "password", "Ert"));
	}

	private static class UserData {
		private String login;
		private String password;
		private String nickname;

		public UserData(String login, String password, String nickname) {
			this.login = login;
			this.password = password;
			this.nickname = nickname;
		}

		public void setLogin(String login) {
			this.login = login;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public void setNickname(String nickname) {
			this.nickname = nickname;
		}
	}
}
