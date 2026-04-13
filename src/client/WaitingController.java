package client;

import common.Modelos.Message;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class WaitingController {

    @FXML
    private AnchorPane rootPane;

    @FXML
    public void initialize() {
        NetworkManager.getInstance().setMessageHandler(this::handleServerMessage);
    }

    private void handleServerMessage(Message msg) {
        if (msg.getType() == null) return;

        // ¡Cuando el servidor encuentra a otro jugador!
        if (msg.getType().equals("MATCH_FOUND")) {
            System.out.println("¡Oponente encontrado! Iniciando partida...");
            goToGame();
        }
    }

    private void goToGame() {
        Platform.runLater(() -> {
            try {
                // Aquí por fin llamamos al FXML principal del juego
                FXMLLoader loader = new FXMLLoader(getClass().getResource("gameDCD.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) rootPane.getScene().getWindow();
                stage.setScene(new Scene(root));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}