package ui;

import javafx.scene.Scene;
import javafx.stage.Stage;
import controller.LeaderboardController;
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
    public static void showLeaderboard() {
        LeaderboardPanel p = new LeaderboardPanel();

        // controller handles back button -> goes back to quiz builder
        new LeaderboardController(p, Navigator::showQuizBuilder);

        stage.setScene(new Scene(p, 1280, 720));
        stage.setTitle("Leaderboard");
        stage.show();
    }
}
