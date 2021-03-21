package ru.bulldog.justchat.server.storage;

import org.jetbrains.annotations.Nullable;
import ru.bulldog.justchat.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.sql.*;

public class DataBase implements Closeable {

	public final static Logger LOGGER = new Logger(DataBase.class);
	private static DataBase instance;

	@Nullable
	public static DataBase getInstance() {
		try {
			if (instance == null) {
				instance = new DataBase();
			}
		} catch (Exception ex) {
			LOGGER.error("Database initialization error.");
		}
		return instance;
	}

	private final Connection connection;
	private final Statement statement;

	private DataBase() throws Exception {
		Class.forName("org.sqlite.JDBC");
		connection = DriverManager.getConnection("jdbc:sqlite:main.db");
		statement = connection.createStatement();
	}

	public PreparedStatement prepareStatement(String sql) throws SQLException {
		return connection.prepareStatement(sql);
	}

	public Statement getStatement() {
		return statement;
	}

	public void enableAutocommit() throws SQLException {
		connection.setAutoCommit(true);
	}

	public void disableAutocommit() throws SQLException {
		connection.setAutoCommit(false);
	}

	@Override
	public void close() throws IOException {
		try {
			statement.close();
			connection.close();
		} catch (SQLException ex) {
			LOGGER.error("Database connection close error", ex);
		}
	}
}
