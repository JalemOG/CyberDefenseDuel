package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

// Clase principal del servidor.
// Se encarga de aceptar conexiones de clientes y crear un hilo (ClientHandler)
// para cada uno de ellos.
public class Server {

    // Puerto en el que el servidor escucha conexiones.
    private static final int PORT = 5000;

    // Método principal del servidor.
    // Inicializa los componentes principales y mantiene el servidor escuchando conexiones.
    public static void main(String[] args) {

        // Se crea el administrador de matchmaking (cola de jugadores).
        MatchManager matchManager = new MatchManager();

        // Se crea el administrador de base de datos (usuarios y estadísticas).
        DatabaseManager databaseManager = new DatabaseManager();

        System.out.println("Servidor iniciado en puerto " + PORT);

        // Se crea el ServerSocket para escuchar conexiones entrantes.
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            // Bucle infinito para aceptar múltiples clientes.
            while (true) {

                // Espera hasta que un cliente se conecte.
                Socket clientSocket = serverSocket.accept();
                System.out.println("Cliente conectado: " + clientSocket.getInetAddress());

                // Se crea un handler para ese cliente, pasando referencias compartidas.
                ClientHandler clientHandler = new ClientHandler(clientSocket, matchManager, databaseManager);

                // Se inicia el hilo para manejar al cliente de forma independiente.
                clientHandler.start();
            }

        } catch (IOException e) {
            System.out.println("Error en el servidor: " + e.getMessage());
        }
    }
}