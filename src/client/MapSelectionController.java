package client;

import common.Modelos.Message;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class MapSelectionController {

    @FXML
    private AnchorPane rootPane;

    @FXML
    public void initialize() {
        NetworkManager.getInstance().setMessageHandler(this::handleServerMessage);
    }

    private void handleServerMessage(Message msg) {
        if (msg.getType() == null) return;

        // Si el servidor nos pone en espera, cambiamos a la pantalla de "Waiting..."
        if (msg.getType().equals("WAITING")) {
            System.out.println("Entrando a la cola de espera...");
            goToWaitingScreen();
        } 
    }

    @FXML
    public void botonDCD(ActionEvent event) {
        // En un juego avanzado, aquí guardaríamos el mapa elegido. 
        // Por ahora, solo le decimos al servidor que queremos jugar.
        NetworkManager.getInstance().sendMessage(new Message("QUEUE"));
    }

    @FXML
    public void botonPBC(ActionEvent event) {
        NetworkManager.getInstance().sendMessage(new Message("QUEUE"));
    }

    private void goToWaitingScreen() {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("waitingforplayers.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) rootPane.getScene().getWindow();
                stage.setScene(new Scene(root));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}