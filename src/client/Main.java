package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 1. Conectarse al servidor apenas arranca el juego
        NetworkManager.getInstance().connect("localhost", 8000);

        Parent root = FXMLLoader.load(getClass().getResource("loginwindow.fxml"));
        primaryStage.setTitle("Cyber Defense Duel");
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(true);
        primaryStage.show();
    }

    @Override
    public void stop() {
        // 2. Desconectarse limpiamente si el usuario cierra la ventana (la 'X')
        NetworkManager.getInstance().disconnect();
    }

    public static void main(String[] args) { 
        launch(args); 
    }
}