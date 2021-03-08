package ru.bulldog.justchat.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class MainController {

	@FXML
	public TextArea txtChatArea;
	@FXML
	public TextField txtMessage;

	public void txtFieldSendMessage(KeyEvent keyEvent) {
		if (keyEvent.getCode() == KeyCode.ENTER) {
			sendMessage();
		}
	}

	public void btnSendMessage() {
		sendMessage();
	}

	private void sendMessage() {
		String message = txtMessage.getText().trim();
		if (!message.equals("")) {
			txtChatArea.appendText(message + "\n");
		}
		txtMessage.requestFocus();
		txtMessage.clear();
	}
}
