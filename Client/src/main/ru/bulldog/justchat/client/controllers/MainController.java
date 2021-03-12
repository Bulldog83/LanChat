package ru.bulldog.justchat.client.controllers;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ru.bulldog.justchat.Logger;
import ru.bulldog.justchat.client.network.ClientNetworkHandler;
import ru.bulldog.justchat.client.network.MessageListener;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.ResourceBundle;

public class MainController implements Initializable, MessageListener {

	private final static Logger LOGGER = new Logger(MainController.class);

	@FXML
	public TextArea txtChatArea;
	@FXML
	public TextField txtMessage;
	@FXML
	public TextField txtServer;
	@FXML
	public TextField txtLogin;
	@FXML
	public PasswordField txtPassword;
	@FXML
	public Label labelNickname;
	@FXML
	public HBox loginForm;
	@FXML
	public HBox chatForm;
	@FXML
	public ListView<String> usersList;

	private ClientNetworkHandler networkHandler;
	private SimpleDateFormat dateFormat;
	private Stage registrationStage;
	private Stage mainStage;

	public void setStage(Stage mainStage) {
		this.mainStage = mainStage;
	}

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

	public void addPrivate(MouseEvent mouseEvent) {
		if (mouseEvent.getButton() == MouseButton.PRIMARY) {
			String addressee = usersList.getSelectionModel().getSelectedItem();
			if (addressee != null) {
				txtMessage.setText("/msg " + addressee + " ");
				txtMessage.requestFocus();
			}
		}
	}

	public void openRegistrationWindow(ActionEvent actionEvent) {
		Platform.runLater(() -> {
			initRegistrationWindow();
			if (registrationStage != null) {
				double posX = mainStage.getX() + mainStage.getWidth() / 2.0 - registrationStage.getWidth() / 2.0;
				double posY = mainStage.getY() + mainStage.getHeight() / 2.0 - registrationStage.getHeight() / 2.0;
				registrationStage.setX(posX);
				registrationStage.setY(posY);
				registrationStage.show();
			}
		});
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
		if (message.startsWith("/")) {
			processSystemData(message);
		} else {
			String timeStamp = dateFormat.format(new Date());
			txtChatArea.appendText(timeStamp + " " + message + "\n");
		}
	}

	@Override
	public void onJoinServer(String nickName) {
		labelNickname.setText(nickName);
		Platform.runLater(() -> {
			loginForm.setVisible(false);
			chatForm.setVisible(true);
		});
	}

	private void processSystemData(String message) {
		if (message.startsWith("/clientjoin")) {
			String nickName = message.substring(12);
			Platform.runLater(() -> usersList.getItems().add(nickName));
			onMessageReceived(nickName + " join.");
		}
		if (message.startsWith("/clientleave")) {
			String nickName = message.substring(13);
			Platform.runLater(() -> usersList.getItems().remove(nickName));
			onMessageReceived(nickName + " leave.");
		}
		if (message.startsWith("/users")) {
			String[] clients = message.substring(7).split(":");
			Platform.runLater(() -> usersList.getItems().setAll(clients));
		}
	}

	private void onLeaveServer() {
		networkHandler.onLeaveServer();
		Platform.runLater(() -> {
			usersList.getItems().clear();
			loginForm.setVisible(true);
			chatForm.setVisible(false);
		});
	}

	public void onStageClosed() {
		networkHandler.onLeaveServer();
	}

	private void initRegistrationWindow() {
		if (registrationStage != null) return;
		try {
			URL fxmlLocation = getClass().getResource("../fxml/registration_window.fxml");
			if (fxmlLocation == null) {
				throw new IOException("FXML load error.");
			}
			FXMLLoader loader = new FXMLLoader(fxmlLocation);
			Parent root = loader.load();
			registrationStage = new Stage();
			registrationStage.initOwner(mainStage);
			registrationStage.setTitle("JustChat: Registration");
			registrationStage.setScene(new Scene(root, 340.0, 250.0));
			registrationStage.setWidth(356.0);
			registrationStage.setHeight(289.0);
			registrationStage.initModality(Modality.APPLICATION_MODAL);
			registrationStage.setResizable(false);
			RegistrationController controller = loader.getController();
			controller.setNetworkHandler(networkHandler);
			controller.setStage(registrationStage);
			controller.setMainController(this);
		} catch (IOException ex) {
			LOGGER.error("Can't create window", ex);
		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		networkHandler = new ClientNetworkHandler(this);
		dateFormat = networkHandler.getDateFormat();
	}
}
