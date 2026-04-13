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

public class CharacterSelectionController {

    @FXML
    private AnchorPane rootPane;

    @FXML
    public void initialize() {
        // Redirigimos los mensajes del servidor hacia esta nueva ventana
        NetworkManager.getInstance().setMessageHandler(this::handleServerMessage);
    }

    private void handleServerMessage(Message msg) {
        if (msg.getType() == null) return;

        // Si el servidor confirma que guardó el avatar, pasamos a la siguiente pantalla
        if (msg.getType().equals("AVATAR_OK")) {
            System.out.println("Avatar confirmado por el servidor.");
            goToMapSelection();
        }
    }

    // --- Botones de los personajes ---
    @FXML
    public void botonCaptainFirewall(ActionEvent event) { enviarAvatar("Captain Firewall"); }

    @FXML
    public void botonByteNinja(ActionEvent event) { enviarAvatar("Byte Ninja"); }

    @FXML
    public void botonMalwareMuncher(ActionEvent event) { enviarAvatar("Malware Muncher"); }

    @FXML
    public void botonCryptoLlama(ActionEvent event) { enviarAvatar("Crypto Llama"); }

    @FXML
    public void botonPacketPirate(ActionEvent event) { enviarAvatar("Packet Pirate"); }

    @FXML
    public void botonNullPointerPaladin(ActionEvent event) { enviarAvatar("Null Pointer Paladin"); }

    // --- Lógica de Red ---
    private void enviarAvatar(String avatarName) {
        Message msg = new Message("SET_AVATAR");
        // Asegúrate de que tu clase Message tenga este método o cámbialo al que uses para guardar strings
        msg.setAvatar(avatarName); 
        NetworkManager.getInstance().sendMessage(msg);
    }

    // --- Navegación ---
    private void goToMapSelection() {
        // Como esto viene del hilo de red, usamos Platform.runLater para manipular la vista
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("mapselection.fxml"));
                Parent root = loader.load();

                // Obtenemos la ventana actual gracias al rootPane
                Stage stage = (Stage) rootPane.getScene().getWindow();
                stage.setScene(new Scene(root));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}