package ui;

import javafx.scene.Scene;
import javafx.stage.Stage;

public class Navigator {
    private static Stage stage;

    public static void init(Stage primaryStage) {
        stage = primaryStage;
    }

    public static void showQuizBuilder() {
        stage.setScene(new Scene(new QuestionCrudPanel(), 1280, 720));
        stage.setTitle("Quiz Builder");
        stage.show();
    }

    public static void showTakeQuiz() {
        stage.setScene(new Scene(new TakeQuizPanel(), 1280, 720));
        stage.setTitle("Take Quiz");
        stage.show();
    }
}
