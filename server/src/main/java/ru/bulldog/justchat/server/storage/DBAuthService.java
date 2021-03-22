package ru.bulldog.justchat.server.storage;

import org.sqlite.core.DB;
import ru.bulldog.justchat.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBAuthService implements AuthService, Closeable {

	private final static Logger LOGGER = new Logger(DBAuthService.class);

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
		if (users.containsKey(login)) return false;
		LOGGER.info("Try to register new user: " + login);
		UserData newUser = new UserData(login, password, nickname);
		if (dbService.saveUser(newUser)) {
			users.put(login, newUser);
			return true;
		}
		return false;
	}

	@Override
	public boolean deleteUser(String login) {
		if (!users.containsKey(login)) return false;
		UserData user = users.get(login);
		if (dbService.deleteUser(user)) {
			users.remove(login);
			return true;
		}
		return false;
	}

	@Override
	public void close() throws IOException {
		dbService.close();
	}
}
