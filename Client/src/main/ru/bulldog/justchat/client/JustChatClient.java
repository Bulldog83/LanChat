package ru.bulldog.justchat.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.bulldog.justchat.Logger;
import ru.bulldog.justchat.client.controllers.MainController;

import java.io.IOException;

public class JustChatClient extends Application {

	private final static Logger LOGGER = new Logger(JustChatClient.class);

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/main_window.fxml"));
			Parent root = loader.load();
			MainController controller = loader.getController();
			primaryStage.setTitle("JustChat");
			primaryStage.setScene(new Scene(root, 1024, 768));
			primaryStage.setOnCloseRequest(event -> controller.onStageClosed());
			primaryStage.show();
		} catch (IOException ex) {
			LOGGER.error("Create window error", ex);
			Platform.exit();
		}
	}
}
