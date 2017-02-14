package ru.splat.fx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    public static void main(String[] args) throws Exception {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        String fxmlFile = "/fxml/hello.fxml";
        FXMLLoader loader = new FXMLLoader();
        Parent root = (Parent) loader.load(getClass().getResourceAsStream(fxmlFile));
        stage.setTitle("Test Boot");
        stage.setScene(new Scene(root));
        Controller controller = loader.getController();
        controller.stage = stage;
        controller.stage.setOnCloseRequest(event -> {
            controller.interruptThreads();
        });
        stage.show();
    }
}