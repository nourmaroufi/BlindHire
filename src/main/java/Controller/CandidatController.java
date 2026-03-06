package Controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import Model.Candidat;
import Service.CandidatService;

import java.sql.SQLException;
import java.util.List;

public class CandidatController {

    @FXML
    private VBox candidatContainer;

    private CandidatService candidatService = new CandidatService();

    @FXML
    public void initialize() {
        loadCandidats();
    }

    private void loadCandidats() {
        candidatContainer.getChildren().clear();

        try {
            List<Candidat> candidats = candidatService.afficherAll();

            for (Candidat candidat : candidats) {
                candidatContainer.getChildren().add(createCandidatCard(candidat));
            }

        } catch (SQLException e) {
            showAlert("Error", "Failed to load candidats: " + e.getMessage());
        }
    }

    private HBox createCandidatCard(Candidat candidat) {

        HBox card = new HBox(20);
        card.getStyleClass().add("interview-card");

        // ID
        TextField idField = new TextField(String.valueOf(candidat.getId_candidat()));
        idField.setPrefWidth(100);
        idField.setDisable(true);

        // Score
        TextField scoreField = new TextField(String.valueOf(candidat.getScore()));
        scoreField.setPrefWidth(150);

        // Status
        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.getItems().addAll("Pending", "Accepted", "Rejected");
        statusBox.setValue(candidat.getStatus());
        statusBox.setPrefWidth(150);

        // Buttons
        Button updateBtn = new Button("Update");
        updateBtn.setStyle("-fx-background-color: #26d0ce; -fx-text-fill: white; -fx-background-radius: 8;");
        updateBtn.setOnAction(e -> {
            try {
                int score = Integer.parseInt(scoreField.getText());
                String status = statusBox.getValue();

                candidatService.update(candidat.getId_candidat(), score, status);
                showAlert("Success", "Candidat updated!");
                loadCandidats();

            } catch (Exception ex) {
                showAlert("Error", ex.getMessage());
            }
        });

        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white; -fx-background-radius: 8;");
        deleteBtn.setOnAction(e -> {
            try {
                candidatService.delete(candidat.getId_candidat());
                loadCandidats();
            } catch (SQLException ex) {
                showAlert("Error", ex.getMessage());
            }
        });

        HBox actionBox = new HBox(10, updateBtn, deleteBtn);
        actionBox.setPrefWidth(200);

        card.getChildren().addAll(idField, scoreField, statusBox, actionBox);

        return card;
    }

    @FXML
    private void openAddCandidatForm() {
        showAlert("Info", "You can implement Add Candidat form like Interview.");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}