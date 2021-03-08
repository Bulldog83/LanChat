package ru.bulldog.justchat;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class JustChatClient extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		try {
			Parent root = FXMLLoader.load(getClass().getResource("fxml/main_window.fxml"));
			primaryStage.setTitle("JustChat");
			primaryStage.setScene(new Scene(root, 1024, 768));
			primaryStage.show();
		} catch (IOException ex) {
			System.out.println(ex.getMessage());
		}
	}
}
