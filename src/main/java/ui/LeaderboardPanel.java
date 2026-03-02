package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import models.LeaderboardRow;
import models.skill;

public class LeaderboardPanel extends StackPane {

    public final Button btnBack = new Button("← Back");
    public final ComboBox<skill> skillCombo = new ComboBox<>();
    public final Button btnLoad = new Button("Load Leaderboard");
    public final Label lblStatus = new Label("Ready");

    public final TableView<LeaderboardRow> table = new TableView<>();

    public LeaderboardPanel() {
        // background
        setStyle("-fx-background-color: #F6F9FF;");

        Pane bubbles = bubbleLayer();
        VBox content = new VBox(16);
        content.setPadding(new Insets(26, 30, 30, 30));

        content.getChildren().addAll(header(), filtersCard(), tableCard());
        VBox.setVgrow(content.getChildren().get(2), Priority.ALWAYS);

        getChildren().addAll(bubbles, content);

        setupTable();
        styleControls();
        skillCombo.setCellFactory(cb -> new ListCell<>() {
            @Override protected void updateItem(skill item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });
        skillCombo.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(skill item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });
    }

    private Pane bubbleLayer() {
        Pane p = new Pane();
        p.setMouseTransparent(true);

        Circle c1 = new Circle(180, Color.web("#DDEBFF"));
        c1.setOpacity(0.6);
        c1.setLayoutX(120);
        c1.setLayoutY(110);

        Circle c2 = new Circle(220, Color.web("#E7E3FF"));
        c2.setOpacity(0.55);
        c2.setLayoutX(1040);
        c2.setLayoutY(130);

        Circle c3 = new Circle(260, Color.web("#DFF7F1"));
        c3.setOpacity(0.45);
        c3.setLayoutX(240);
        c3.setLayoutY(740);

        p.getChildren().addAll(c1, c2, c3);
        return p;
    }

    private HBox header() {
        HBox row = new HBox(14);
        row.setAlignment(Pos.CENTER_LEFT);

        btnBack.setStyle("-fx-background-color: transparent; -fx-text-fill: #2F6FE6; -fx-font-weight: 800; -fx-cursor: hand;");

        VBox titles = new VBox(3);
        Label title = new Label("Leaderboard");
        title.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 30));
        title.setTextFill(Color.web("#1D2B5A"));

        Label sub = new Label("Classement par skill (score le plus élevé en premier)");
        sub.setFont(Font.font("Segoe UI", 13));
        sub.setTextFill(Color.web("#6E7AA8"));

        titles.getChildren().addAll(title, sub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        lblStatus.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        lblStatus.setTextFill(Color.web("#6E7AA8"));
        lblStatus.setPadding(new Insets(6, 10, 6, 10));
        lblStatus.setStyle(pillStyle("#FFFFFF", "#DDE6FF"));

        row.getChildren().addAll(btnBack, titles, spacer, lblStatus);
        return row;
    }

    private VBox filtersCard() {
        VBox card = cardBox();
        card.setPadding(new Insets(16));

        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);

        Label l = new Label("Skill");
        l.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        l.setTextFill(Color.web("#1D2B5A"));

        skillCombo.setPromptText("Choose a skill...");
        skillCombo.setPrefWidth(360);
        skillCombo.setPrefHeight(38);

        btnLoad.setPrefHeight(38);
        btnLoad.setStyle(primaryButtonStyle());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        row.getChildren().addAll(l, skillCombo, btnLoad, spacer);
        card.getChildren().add(row);

        return card;
    }

    private VBox tableCard() {
        VBox card = cardBox();
        card.setPadding(new Insets(16));
        card.setSpacing(10);

        Label t = new Label("Ranking");
        t.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        t.setTextFill(Color.web("#1D2B5A"));

        table.setPrefHeight(520);

        card.getChildren().addAll(t, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        return card;
    }

    private VBox cardBox() {
        VBox box = new VBox();
        box.setStyle(
                "-fx-background-color: rgba(255,255,255,0.92);" +
                        "-fx-background-radius: 18;" +
                        "-fx-border-radius: 18;" +
                        "-fx-border-color: #DDE6FF;"
        );
        box.setEffect(new DropShadow(18, Color.rgb(0,0,0,0.08)));
        return box;
    }

    private void setupTable() {
        TableColumn<LeaderboardRow, Number> colRank = new TableColumn<>("#");
        colRank.setCellValueFactory(cd ->
                new javafx.beans.property.SimpleIntegerProperty(table.getItems().indexOf(cd.getValue()) + 1));
        colRank.setMaxWidth(80);
        colRank.setMinWidth(60);

        TableColumn<LeaderboardRow, Number> colUser = new TableColumn<>("User ID");
        colUser.setCellValueFactory(cd ->
                new javafx.beans.property.SimpleIntegerProperty(cd.getValue().getUserId()));
        colUser.setMinWidth(200);

        TableColumn<LeaderboardRow, String> colScore = new TableColumn<>("Score (%)");
        colScore.setCellValueFactory(cd ->
                new javafx.beans.property.SimpleStringProperty(
                        cd.getValue().getScore() == null ? "" : cd.getValue().getScore().toPlainString()
                ));
        colScore.setMinWidth(200);

        table.getColumns().setAll(colRank, colUser, colScore);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Label empty = new Label("No results");
        empty.setTextFill(Color.web("#6E7AA8"));
        empty.setFont(Font.font("Segoe UI", 13));
        table.setPlaceholder(empty);

        // nicer row height
        table.setFixedCellSize(44);
    }

    private void styleControls() {
        // combo aesthetics
        skillCombo.setStyle(
                "-fx-background-radius: 12;" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-color: #DDE6FF;" +
                        "-fx-padding: 6 10;"
        );

        // table aesthetics (header + rows)
        table.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-control-inner-background: rgba(255,255,255,0.75);" +
                        "-fx-control-inner-background-alt: rgba(245,248,255,0.75);" +
                        "-fx-table-cell-border-color: transparent;"
        );
    }

    private String pillStyle(String bg, String border) {
        return "-fx-background-color: " + bg + ";" +
                "-fx-background-radius: 999;" +
                "-fx-border-radius: 999;" +
                "-fx-border-color: " + border + ";";
    }

    private String primaryButtonStyle() {
        return "-fx-background-color: linear-gradient(to bottom, #3B82F6, #2F6FE6);" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: 800;" +
                "-fx-background-radius: 999;" +
                "-fx-padding: 8 16;" +
                "-fx-cursor: hand;";
    }
}