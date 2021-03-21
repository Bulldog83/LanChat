package ru.bulldog.justchat.server.storage;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBAuthService implements AuthService, Closeable {

	private final DataBaseService dbService;
	private final Map<String, UserData> users;

	public DBAuthService() throws RuntimeException {
		this.users = new HashMap<>();
		this.dbService = new DataBaseService();
		loadUsers();
	}

	private void loadUsers() {
		List<UserData> users = dbService.getUsers();
		users.forEach(user -> this.users.put(user.getLogin(), user));
	}

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
			if (user.getLogin().equals(login) ||
				user.getNickname().equals(nickname)) {

				return false;
			}
		}
		UserData newUser = new UserData(login, password, nickname);
		if (dbService.saveUser(newUser)) {
			users.put(login, newUser);
			return true;
		}
		return false;
	}

	@Override
	public void close() throws IOException {
		dbService.close();
	}
}
