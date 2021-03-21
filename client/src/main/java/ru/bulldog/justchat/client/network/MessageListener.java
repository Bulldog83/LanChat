package ru.bulldog.justchat.client.network;

public interface MessageListener {
	void onMessageReceived(String message);
	void onJoinServer(String nickName);
}
