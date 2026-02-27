package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import models.skill;
import models.LeaderboardRow;

public class LeaderboardPanel extends VBox {
    public final Button btnBack = new Button("← Back");
    public final ComboBox<skill> skillCombo = new ComboBox<>();
    public final Button btnLoad = new Button("Load");
    public final Label lblStatus = new Label("Ready");

    public final TableView<LeaderboardRow> table = new TableView<>();

    public LeaderboardPanel() {
        setPadding(new Insets(22));
        setSpacing(16);
        setStyle("-fx-background-color: #F6F9FF;");

        getChildren().addAll(createHeader(), createCard());
        VBox.setVgrow(getChildren().get(1), Priority.ALWAYS);

        setupTable();
        style();
    }

    private HBox createHeader() {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);

        btnBack.setStyle("-fx-background-color: transparent; -fx-text-fill: #2F6FE6; -fx-font-weight: 800; -fx-cursor: hand;");

        VBox titles = new VBox(2);
        Label title = new Label("Leaderboard");
        title.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 26));
        title.setTextFill(Color.web("#1D2B5A"));

        Label sub = new Label("Ranking of users per skill (highest score first)");
        sub.setFont(Font.font("Segoe UI", 13));
        sub.setTextFill(Color.web("#6E7AA8"));
        titles.getChildren().addAll(title, sub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        lblStatus.setTextFill(Color.web("#6E7AA8"));
        lblStatus.setPadding(new Insets(6, 10, 6, 10));
        lblStatus.setStyle("-fx-background-color: white; -fx-background-radius: 999; -fx-border-radius: 999; -fx-border-color: #DDE6FF;");

        row.getChildren().addAll(btnBack, titles, spacer, lblStatus);
        return row;
    }

    private VBox createCard() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 18; -fx-border-radius: 18; -fx-border-color: #DDE6FF;");

        HBox filters = new HBox(10);
        filters.setAlignment(Pos.CENTER_LEFT);

        Label l = new Label("Skill");
        l.setTextFill(Color.web("#1D2B5A"));
        l.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));

        skillCombo.setPromptText("Choose a skill...");
        skillCombo.setPrefWidth(320);

        btnLoad.setPrefHeight(38);
        btnLoad.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #3B82F6, #2F6FE6);" +
                        "-fx-text-fill: white; -fx-font-weight: 800; -fx-background-radius: 999;" +
                        "-fx-padding: 8 16; -fx-cursor: hand;"
        );

        filters.getChildren().addAll(l, skillCombo, btnLoad);

        card.getChildren().addAll(filters, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        return card;
    }

    private void setupTable() {
        TableColumn<LeaderboardRow, Number> colRank = new TableColumn<>("#");
        colRank.setCellValueFactory(cd -> new javafx.beans.property.SimpleIntegerProperty(table.getItems().indexOf(cd.getValue()) + 1));
        colRank.setPrefWidth(60);

        TableColumn<LeaderboardRow, Number> colUser = new TableColumn<>("User ID");
        colUser.setCellValueFactory(cd -> new javafx.beans.property.SimpleIntegerProperty(cd.getValue().getUserId()));
        colUser.setPrefWidth(160);

        TableColumn<LeaderboardRow, String> colScore = new TableColumn<>("Score (%)");
        colScore.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(
                cd.getValue().getScore() == null ? "" : cd.getValue().getScore().toPlainString()
        ));
        colScore.setPrefWidth(160);

        table.getColumns().addAll(colRank, colUser, colScore);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No results"));
    }

    private void style() {
        table.setStyle("-fx-background-color: transparent;");
    }
}