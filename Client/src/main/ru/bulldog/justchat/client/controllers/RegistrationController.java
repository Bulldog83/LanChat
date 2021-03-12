package ru.bulldog.justchat.client.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ru.bulldog.justchat.client.network.ClientNetworkHandler;
import ru.bulldog.justchat.client.network.MessageListener;

public class RegistrationController implements MessageListener {

	@FXML
	public TextField txtNickname;
	@FXML
	public TextField txtLogin;
	@FXML
	public TextField txtServer;
	@FXML
	public PasswordField txtPassword;
	@FXML
	public Label labStatus;

	private ClientNetworkHandler networkHandler;
	private MainController mainController;
	private Stage currentStage;

	public void setNetworkHandler(ClientNetworkHandler networkHandler) {
		this.networkHandler = networkHandler;
	}

	public void setMainController(MainController mainController) {
		this.mainController = mainController;
	}

	public void setStage(Stage stage) {
		this.currentStage = stage;
	}

	public void doRegistration(ActionEvent actionEvent) {
		String address = txtServer.getText().trim();
		if (address.equals("")) {
			labStatus.setText("Address can't be empty.");
			return;
		}
		String login = txtLogin.getText().trim();
		if (login.equals("")) {
			labStatus.setText("Login can't be empty.");
			return;
		}
		String password = txtPassword.getText().trim();
		if (password.equals("")) {
			labStatus.setText("Password can't be empty.");
			return;
		}
		String nickName = txtNickname.getText().trim();
		if (nickName.equals("")) {
			labStatus.setText("Nickname can't be empty.");
			return;
		}
		if (networkHandler.doRegistration(this, address, login, password, nickName)) {
			mainController.txtServer.setText(address);
			mainController.txtLogin.setText(login);
			close();
		}
	}

	public void windowClose(ActionEvent actionEvent) {
		close();
	}

	@Override
	public void onMessageReceived(String message) {
		labStatus.setText(message);
	}

	@Override
	public void onJoinServer(String nickName) {}

	private void close() {
		currentStage.hide();
	}
}
