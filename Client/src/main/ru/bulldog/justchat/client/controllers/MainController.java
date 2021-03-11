package ru.bulldog.justchat.client.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import ru.bulldog.justchat.client.network.ClientNetworkHandler;
import ru.bulldog.justchat.client.network.MessageListener;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable, MessageListener {

	@FXML
	public TextArea txtChatArea;
	@FXML
	public TextField txtMessage;
	@FXML
	public TextField txtServer;
	@FXML
	public TextField txtLogin;
	@FXML
	public TextField txtNickname;
	@FXML
	public PasswordField txtPassword;
	@FXML
	public Label labelNickname;
	@FXML
	public HBox loginForm;
	@FXML
	public HBox chatForm;
	@FXML
	public AnchorPane mainWindow;

	private ClientNetworkHandler networkHandler;

	public void txtFieldSendMessage(KeyEvent keyEvent) {
		if (keyEvent.getCode() == KeyCode.ENTER) {
			sendMessage();
		}
	}

	public void btnSendMessage() {
		sendMessage();
	}

	private void sendMessage() {
		txtMessage.requestFocus();
		String message = txtMessage.getText();
		if (networkHandler.sendMessage(message)) {
			if (message.equals("/quit")) {
				onLeaveServer();
			}
			txtMessage.clear();
		}
	}

	public void tryLogin(ActionEvent actionEvent) {
		String address = txtServer.getText().trim();
		String login = txtLogin.getText().trim();
		String password = txtPassword.getText().trim();
		if (address.equals("")) {
			txtChatArea.appendText("Error: address can't be empty.\n");
			return;
		}
		if (login.equals("")) {
			txtChatArea.appendText("Error: login can't be empty.\n");
			return;
		}
		if (password.equals("")) {
			txtChatArea.appendText("Error: password can't be empty.\n");
			return;
		}
		if (networkHandler.joinServer(address, login, password)) {
			txtPassword.clear();
		} else {
			onLeaveServer();
		}
	}

	@Override
	public void onMessageReceived(String message) {
		txtChatArea.appendText(message + "\n");
	}

	@Override
	public void onJoinServer(String nickName) {
		labelNickname.setText(nickName);
		Platform.runLater(() -> {
			loginForm.setVisible(false);
			chatForm.setVisible(true);
		});
	}

	private void onLeaveServer() {
		networkHandler.onLeaveServer();
		Platform.runLater(() -> {
			loginForm.setVisible(true);
			chatForm.setVisible(false);
		});
	}

	public void onStageClosed() {
		networkHandler.onLeaveServer();
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		networkHandler = new ClientNetworkHandler(this);
	}
}
