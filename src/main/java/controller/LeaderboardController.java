package controller;

import javafx.collections.FXCollections;
import models.skill;
import services.skillService;
import services.scoreService;
import ui.LeaderboardPanel;

import java.sql.SQLException;

public class LeaderboardController {
    private final LeaderboardPanel view;
    private final skillService skillService;
    private final scoreService scoreService;

    private final Runnable onBack;

    public LeaderboardController(LeaderboardPanel view, Runnable onBack) {
        this.view = view;
        this.onBack = onBack;
        this.skillService = new skillService();
        this.scoreService = new scoreService();

        init();
    }

    private void init() {
        loadSkills();

        view.btnBack.setOnAction(e -> onBack.run());
        view.btnLoad.setOnAction(e -> loadLeaderboard());
    }

    private void loadSkills() {
        try {
            view.skillCombo.setItems(FXCollections.observableArrayList(skillService.getAllskills()));
        } catch (SQLException e) {
            view.lblStatus.setText("DB error loading skills");
            e.printStackTrace();
        }
    }

    private void loadLeaderboard() {
        skill sk = view.skillCombo.getValue();
        if (sk == null) { view.lblStatus.setText("Select a skill"); return; }

        try {
            view.table.setItems(FXCollections.observableArrayList(
                    scoreService.getLeaderboardBySkill(sk.getIdSkill())
            ));
            view.lblStatus.setText("Loaded: " + view.table.getItems().size());
        } catch (SQLException e) {
            view.lblStatus.setText("DB error loading leaderboard");
            e.printStackTrace();
        }
    }
}