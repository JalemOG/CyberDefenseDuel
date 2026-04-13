package server;

import com.google.gson.Gson;

import common.Modelos.Message;
import java.io.*;
import java.net.*;
import common.Estructuras.ListaDoblementeEnlazada;
import common.Estructuras.Nodo;
import common.Estructuras.ListaEnlazada;
import java.util.Scanner;


public class Server {

    private static final int PORT = 8000;
    private static ServerSocket serverSocket;
    private static volatile boolean isRunning = true;
    private static final Gson gson = new Gson();
    
    private static final ListaDoblementeEnlazada<ClientHandler> clients = new ListaDoblementeEnlazada<>();
    
    public static void main(String[] args) {
        // 1. Iniciar hilo de la consola para cerrar el servidor
        startAdminConsole();

        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("========================================");
            System.out.println("🛡️ CYBER DEFENSE DUEL - SERVIDOR");
            System.out.println("Puerto: " + PORT);
            System.out.println("Estado: ESPERANDO JUGADORES...");
            System.out.println("========================================");
            System.out.println("(Escribe 'exit' en cualquier momento para apagar)");

            while (isRunning) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    if (!isRunning) break;

                    System.out.println("📡 Nueva conexión desde: " + clientSocket.getInetAddress());
                    
                    // Crear un manejador para este cliente específico
                    ClientHandler handler = new ClientHandler(clientSocket);
                    synchronized (clients) {
                        clients.agregar(handler); 
                    }
                    new Thread(handler).start();

                } catch (IOException e) {
                    if (isRunning) {
                        System.err.println("❌ Error al aceptar conexión: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("❌ No se pudo iniciar el servidor: " + e.getMessage());
        } finally {
            shutdown();
        }
    }

    private static void startAdminConsole() {
        Thread adminThread = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (isRunning) {
                String input = scanner.nextLine();
                if (input.equalsIgnoreCase("exit") || input.equalsIgnoreCase("stop")) {
                    isRunning = false;
                    System.out.println("⚠️  Cerrando servidor...");
                    try {
                        if (serverSocket != null) serverSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
            scanner.close();
        });
        adminThread.setDaemon(true);
        adminThread.start();
    }

    private static void shutdown() {
        System.out.println("🛑 Apagando socket del servidor...");
        synchronized (clients) {
          
            Nodo<ClientHandler> actual = clients.getCabeza(); 
            
            while (actual != null) {
                actual.getValor().closeConnection();
                actual = actual.getSiguiente();
            }
        }
        System.out.println("✅ Servidor fuera de línea.");
    }

    // --- CLASE INTERNA PARA MANEJAR CADA CLIENTE ---
    public static class ClientHandler implements Runnable {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        
        private String username; 
        private GameSession session;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            try {
                this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                this.out = new PrintWriter(socket.getOutputStream(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                String rawJson;
                while ((rawJson = in.readLine()) != null) {
                    Message msg = gson.fromJson(rawJson, Message.class);
                    processMessage(msg);
                }
            } catch (IOException e) {
                System.out.println("🔌 Jugador desconectado.");
            } finally {
                closeConnection();
            }
        }

        private void processMessage(Message msg) {
            if (msg.getType() == null) return;

            switch (msg.getType()) {
                case "LOGIN":
                    handleLogin(msg);
                    break;
                case "REGISTER":
                    handleRegister(msg);
                    break;
                case "SET_AVATAR":   //
                    handleSetAvatar(msg);
                    break;
                case "QUEUE":
                    MatchManager.joinQueue(this); // Lo mandamos a la cola real
                    break;
                case "SYNC_STATS":
                case "I_DIED":
                    // Si el jugador hace algo en la partida, se lo pasamos a la GameSession
                    if (session != null) {
                        session.relayMessage(msg, this);
                    }
                    break;
            }
        }

        private void handleLogin(Message msg) {
            System.out.println("🔑 Intento de login: " + msg.getUsername());
            this.username = msg.getUsername(); // <--- GUARDAMOS EL NOMBRE EN LA MEMORIA DEL HILO
            send(new Message("LOGIN_OK"));
        }

        private void handleRegister(Message msg) {
            System.out.println("📝 Registrando nuevo usuario: " + msg.getUsername());
            
            File file = new File("database.json");
            DatabaseWrapper db;

            try {
                // 1. Leer base de datos actual
                if (file.exists()) {
                    BufferedReader reader = new BufferedReader(new FileReader(file));
                    db = gson.fromJson(reader, DatabaseWrapper.class);
                    reader.close();
                } else {
                    db = new DatabaseWrapper();
                }
                
                ListaEnlazada<User> listaUsuarios = db.getListaUsuarios();
                // 2. Verificar si el usuario ya existe
                boolean exists = false;
                for (int i = 0; i < listaUsuarios.getSize(); i++) {
                    User usuarioActual = listaUsuarios.obtener(i);
                    if (usuarioActual.username.equals(msg.getUsername())) {
                        exists = true;
                        break;
                    }
                }

                if (exists) {
                    send(new Message("AUTH_ERROR")); // O un mensaje de "USER_EXISTS"
                    return;
                }

                // 3. Añadimos el nuevo usuario a tu lista
                User newUser = new User(msg.getUsername(), msg.getPassword());
                listaUsuarios.agregar(newUser);

                // 4. Actualizamos el Wrapper y guardamos
                db.actualizarDesdeLista(listaUsuarios);

                FileWriter writer = new FileWriter(file);
                gson.toJson(db, writer);
                writer.close();

                this.username = msg.getUsername(); // Recordar quién es en este hilo
                System.out.println("💾 Usuario guardado con éxito en database.json");
                send(new Message("REGISTER_OK"));

            } catch (IOException e) {
                System.err.println("❌ Error al acceder a la base de datos: " + e.getMessage());
            }
        }
        
        private void handleSetAvatar(Message msg) {
            // 1. Log en la consola del servidor para saber qué eligió el usuario
            System.out.println("👤 Jugador [" + this.username + "] seleccionó el avatar: " + msg.getAvatar());

            // 2. En una versión final, aquí haríamos: 
            // user.setAvatar(msg.getAvatar()); database.save(user);
            // Por ahora, solo confirmamos que recibimos la elección.

            // 3. Crear el mensaje de confirmación
            Message response = new Message("AVATAR_OK");
            
            // 4. Enviar la respuesta al cliente
            send(response);
            
            System.out.println("✅ Confirmación AVATAR_OK enviada al cliente.");
        }
        
        public String getUsername() { return username; }
        public void setSession(GameSession session) { this.session = session; }
        
        public void send(Message msg) {
            out.println(gson.toJson(msg));
        }

        public void closeConnection() {
            try {
                synchronized (clients) {
                    clients.eliminar(this); // Cambia "eliminar" por tu método para borrar nodos (ej. remover, delete)
                }
                if (socket != null) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}



class DatabaseWrapper {
    // Usamos un arreglo nativo (User[]) estrictamente como puente para que Gson entienda el JSON
    User[] users;

    // Método para convertir el arreglo a tu estructura personalizada
    public ListaEnlazada<User> getListaUsuarios() {
        ListaEnlazada<User> lista = new ListaEnlazada<>();
        if (users != null) {
            for (int i = 0; i < users.length; i++) {
                lista.agregar(users[i]);
            }
        }
        return lista;
    }

    // Método para convertir tu estructura de vuelta a arreglo antes de que Gson lo guarde
    public void actualizarDesdeLista(ListaEnlazada<User> lista) {
        users = new User[lista.getSize()];
        for (int i = 0; i < lista.getSize(); i++) {
            users[i] = lista.obtener(i);
        }
    }
}

class User {
    String username;
    String password;
    String avatar = "None";
    Stats stats = new Stats();

    public User(String u, String p) { 
        this.username = u; 
        this.password = p; 
    }
}

class Stats {
    int totalScore = 0, gamesPlayed = 0, networkXp = 0, malwareXp = 0, cryptoXp = 0;
}