package client;

import com.google.gson.Gson;
import common.Modelos.Message;
import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.function.Consumer;

public class NetworkManager {
    
    // Implementamos el patrón Singleton para que cualquier controlador 
    // pueda acceder a la misma conexión sin tener que pasar la variable
    private static NetworkManager instance;
    
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private final Gson gson;

    // Esta función nos permitirá decirle a la interfaz gráfica qué hacer cuando llegue un mensaje
    private Consumer<Message> messageHandler;

    private NetworkManager() {
        gson = new Gson();
    }

    public static NetworkManager getInstance() {
        if (instance == null) {
            instance = new NetworkManager();
        }
        return instance;
    }

    public void connect(String host, int port) {
        try {
            socket = new Socket(host, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            System.out.println("Conectado al servidor en " + host + ":" + port);

            // Creamos un hilo en segundo plano (Daemon) para escuchar al servidor
            Thread listener = new Thread(() -> {
                try {
                    String rawMessage;
                    while ((rawMessage = in.readLine()) != null) {
                        Message msg = gson.fromJson(rawMessage, Message.class);
                        
                        // Si la interfaz gráfica configuró un "handler", le pasamos el mensaje
                        if (messageHandler != null) {
                            // Platform.runLater es OBLIGATORIO en JavaFX cuando un hilo 
                            // secundario quiere modificar la interfaz gráfica
                            Platform.runLater(() -> messageHandler.accept(msg));
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Desconectado del servidor.");
                }
            });
            listener.setDaemon(true); // Esto asegura que el hilo muera si cerramos la ventana
            listener.start();

        } catch (IOException e) {
            System.out.println("Error conectando al servidor: No se encontró el servidor.");
        }
    }

    // Método para cambiar qué controlador está escuchando los mensajes actualmente
    public void setMessageHandler(Consumer<Message> handler) {
        this.messageHandler = handler;
    }

    // Método para que los botones envíen cosas al servidor
    public void sendMessage(Message msg) {
        if (out != null) {
            out.println(gson.toJson(msg));
        }
    }

    public void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}