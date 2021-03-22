package ru.bulldog.justchat.server.storage;

import java.io.Closeable;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DataBaseService implements Closeable {

	private final DataBase dataBase;

	public DataBaseService() throws RuntimeException {
		this.dataBase = DataBase.getInstance();
		if (dataBase == null) {
			throw new RuntimeException("Database error");
		}
	}

	public List<UserData> getUsers() {
		List<UserData> users = new ArrayList<>();
		String sql = "SELECT login, password, nickname FROM users";
		Statement statement = dataBase.getStatement();
		try {
			ResultSet resultSet = statement.executeQuery(sql);
			while (resultSet.next()) {
				users.add(new UserData(
					resultSet.getString("login"),
					resultSet.getString("password"),
					resultSet.getString("nickname")
				));
			}
		} catch (SQLException ex) {
			DataBase.LOGGER.error("Data load error", ex);
		}
		return users;
	}

	public boolean saveUser(UserData user) {
		try {
			if (isUserExists(user.getLogin())) {
				System.out.println("User " + user + " exists.");
				String sql = "UPDATE users SET password=?, nickname=? WHERE login=?";
				try (PreparedStatement updateStatement = dataBase.prepareStatement(sql)) {
					updateStatement.setString(1, user.getPassword());
					updateStatement.setString(2, user.getNickname());
					updateStatement.setString(3, user.getLogin());
					return updateStatement.executeUpdate() > 0;
				}
			} else {
				System.out.println("User " + user + " not exists.");
				String sql = "INSERT INTO users (login, password, nickname) VALUES (?, ?, ?)";
				try (PreparedStatement insertStatement = dataBase.prepareStatement(sql)) {
					insertStatement.setString(1, user.getLogin());
					insertStatement.setString(2, user.getPassword());
					insertStatement.setString(3, user.getNickname());
					return insertStatement.executeUpdate() > 0;
				}
			}
		} catch (SQLException ex) {
			DataBase.LOGGER.error("Database error", ex);
		}
		return false;
	}

	public boolean deleteUser(UserData user) {
		if (user == null || !isUserExists(user.getLogin())) return false;
		String sql = "DELETE FROM users WHERE login=? LIMIT 1";
		try (PreparedStatement deleteStatement = dataBase.prepareStatement(sql)) {
			deleteStatement.setString(1, user.getLogin());
			return deleteStatement.executeUpdate() > 0;
		} catch (SQLException ex) {
			DataBase.LOGGER.error("Database error", ex);
		}
		return false;
	}

	public boolean isUserExists(String login) {
		String sql = "SELECT login FROM users WHERE login=?";
		try (PreparedStatement statement = dataBase.prepareStatement(sql)) {
			statement.setString(1, login);
			ResultSet result = statement.executeQuery();
			return result.next();
		} catch (SQLException ex) {
			DataBase.LOGGER.error("Database error", ex);
		}
		return false;
	}

	@Override
	public void close() throws IOException {
		dataBase.close();
	}
}
