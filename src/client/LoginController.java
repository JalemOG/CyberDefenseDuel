package client;

import common.Modelos.Message;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField loginUsernameField;
    @FXML private PasswordField loginPasswordField;
    @FXML private TextField regUsernameField;
    @FXML private PasswordField regPasswordField;

    @FXML
    public void initialize() {
        // Al cargar esta ventana, le decimos al NetworkManager que envíe los mensajes aquí
        NetworkManager.getInstance().setMessageHandler(this::handleServerMessage);
    }

    // Este método escucha lo que responde el servidor
    private void handleServerMessage(Message msg) {
        if (msg.getType() == null) return;

        switch (msg.getType()) {
            case "LOGIN_OK":
            case "REGISTER_OK":
                System.out.println("Autenticación exitosa. Cambiando pantalla...");
                goToCharacterSelection();
                break;
            case "ERROR":
                // Mostrar una alerta visual de JavaFX si el usuario no existe o contraseña es mala
                showAlert("Error del Servidor", msg.getText());
                break;
        }
    }

    @FXML
    public void botonLogin(ActionEvent event) {
        String user = loginUsernameField.getText();
        String pass = loginPasswordField.getText();
        if (user.isEmpty() || pass.isEmpty()) {
            showAlert("Alerta", "Por favor llene todos los campos de Login.");
            return;
        }

        // Armar el JSON de login según la Hoja de Trucos
        Message msg = new Message("LOGIN");
        msg.setUsername(user);
        msg.setPassword(pass);
        NetworkManager.getInstance().sendMessage(msg);
    }

    @FXML
    public void botonRegister(ActionEvent event) {
        String user = regUsernameField.getText();
        String pass = regPasswordField.getText();
        if (user.isEmpty() || pass.isEmpty()) {
            showAlert("Alerta", "Por favor llene todos los campos de Registro.");
            return;
        }

        // Armar el JSON de registro según la Hoja de Trucos
        Message msg = new Message("REGISTER");
        msg.setUsername(user);
        msg.setPassword(pass);
        NetworkManager.getInstance().sendMessage(msg);
    }

    private void goToCharacterSelection() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("characterselection.fxml"));
            Parent root = loader.load();

            // Obtenemos la ventana actual usando cualquier elemento de la vista
            Stage stage = (Stage) loginUsernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}