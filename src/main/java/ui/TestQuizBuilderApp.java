package ui;

import javafx.application.Application;
import javafx.stage.Stage;

public class TestQuizBuilderApp extends Application {
    @Override
    public void start(Stage stage) {
        Navigator.init(stage);
        Navigator.showQuizBuilder();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

