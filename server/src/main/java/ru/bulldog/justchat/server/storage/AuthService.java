package ru.bulldog.justchat.server.storage;

import org.jetbrains.annotations.Nullable;

public interface AuthService {
	@Nullable
	String getNickname(String login, String password);
	boolean registerUser(String login, String password, String nickname);
	boolean deleteUser(String login);
}
