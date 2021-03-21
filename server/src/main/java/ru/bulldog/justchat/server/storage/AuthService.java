package ru.bulldog.justchat.server.storage;

public interface AuthService {
	String getNickname(String login, String password);
	boolean registerUser(String login, String password, String nickname);
}
