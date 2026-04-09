package server;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import model.Message;

// Clase que maneja la conexión individual de un cliente.
// Cada cliente conectado al servidor tiene su propio hilo (Thread).
public class ClientHandler extends Thread {

    // Socket del cliente.
    private final Socket socket;

    // Referencias a componentes del servidor.
    private final MatchManager matchManager;
    private final DatabaseManager databaseManager;

    // Gson para convertir mensajes JSON.
    private final Gson gson = new Gson();

    // Canales de entrada y salida.
    private BufferedReader in;
    private PrintWriter out;

    // Información del cliente conectado.
    private String username;
    private ClientHandler opponent;
    private GameSession session;
    private boolean inQueue = false;

    // Estado actual del jugador durante la partida.
    private int currentHp = 100;
    private int currentScore = 0;
    private int currentLevel = 0;
    private boolean gameOverSent = false;

    // Constructor del handler.
    public ClientHandler(Socket socket, MatchManager matchManager, DatabaseManager databaseManager) {
        this.socket = socket;
        this.matchManager = matchManager;
        this.databaseManager = databaseManager;
    }

    // Devuelve el username o un valor por defecto si no está definido.
    public String getUsername() {
        return username == null ? "JugadorSinNombre" : username;
    }

    // Asigna el oponente.
    public void setOpponent(ClientHandler opponent) {
        this.opponent = opponent;
    }

    // Devuelve el oponente.
    public ClientHandler getOpponent() {
        return opponent;
    }

    // Asigna la sesión de juego.
    public void setSession(GameSession session) {
        this.session = session;
    }

    // Devuelve la sesión de juego.
    public GameSession getSession() {
        return session;
    }

    // Indica si el jugador está en cola.
    public boolean isInQueue() {
        return inQueue;
    }

    // Marca si el jugador está en cola.
    public void setInQueue(boolean inQueue) {
        this.inQueue = inQueue;
    }

    // Devuelve el HP actual.
    public int getCurrentHp() {
        return currentHp;
    }

    // Devuelve el score actual.
    public int getCurrentScore() {
        return currentScore;
    }

    // Devuelve el nivel actual.
    public int getCurrentLevel() {
        return currentLevel;
    }

    // Indica si ya envió GAME_OVER.
    public boolean hasSentGameOver() {
        return gameOverSent;
    }

    // Devuelve el manejador de base de datos.
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    // Reinicia el estado del jugador para una nueva partida.
    public void resetForNewMatch() {
        this.opponent = null;
        this.session = null;
        this.inQueue = false;
        this.currentHp = 100;
        this.currentScore = 0;
        this.currentLevel = 0;
        this.gameOverSent = false;
    }

    // Envía un mensaje JSON al cliente.
    public void sendMessage(Message message) {
        if (out != null) {
            out.println(gson.toJson(message));
        }
    }

    // Envía un mensaje simple (solo tipo).
    private void sendSimple(String type) {
        sendMessage(Message.simple(type));
    }

    // Envía un mensaje de error al cliente.
    private void sendError(String text) {
        Message msg = new Message("ERROR");
        msg.setText(text);
        sendMessage(msg);
    }

    // Método principal del hilo.
    // Escucha continuamente los mensajes del cliente.
    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Envía mensaje de bienvenida.
            Message welcome = new Message("WELCOME");
            welcome.setText("Bienvenido al servidor");
            sendMessage(welcome);

            String raw;

            // Loop principal de escucha.
            while ((raw = in.readLine()) != null) {
                System.out.println("Mensaje recibido: " + raw);
                handleMessage(raw);
            }

        } catch (IOException e) {
            System.out.println("Cliente desconectado.");
        } finally {
            closeConnection();
        }
    }

    // Procesa cada mensaje recibido desde el cliente.
    private void handleMessage(String raw) {
        try {
            Message msg = gson.fromJson(raw, Message.class);

            if (msg == null || msg.getType() == null) {
                sendError("Mensaje JSON inválido");
                return;
            }

            // Dispatcher de tipos de mensajes.
            switch (msg.getType()) {
                case "PING":
                    sendSimple("PONG");
                    break;

                case "REGISTER":
                    handleRegister(msg);
                    break;

                case "LOGIN":
                    handleLogin(msg);
                    break;

                case "SET_AVATAR":
                    handleSetAvatar(msg);
                    break;

                case "QUEUE":
                    handleQueue();
                    break;

                case "STATE":
                    handleState(msg);
                    break;

                case "GAME_OVER":
                    handleGameOver();
                    break;

                default:
                    sendError("Tipo de mensaje no reconocido");
                    break;
            }

        } catch (Exception e) {
            sendError("No se pudo procesar el JSON");
        }
    }

    // Maneja el registro de usuario.
    private void handleRegister(Message msg) {
        if (msg.getUsername() == null || msg.getPassword() == null) {
            sendError("REGISTER inválido");
            return;
        }

        boolean registered = databaseManager.registerUser(msg.getUsername(), msg.getPassword());

        if (registered) {
            username = msg.getUsername();
            sendSimple("REGISTER_OK");
        } else {
            sendError("Usuario ya existe");
        }
    }

    // Maneja el login de usuario.
    private void handleLogin(Message msg) {
        if (msg.getUsername() == null || msg.getPassword() == null) {
            sendError("LOGIN inválido");
            return;
        }

        boolean logged = databaseManager.loginUser(msg.getUsername(), msg.getPassword());

        if (logged) {
            username = msg.getUsername();
            sendSimple("LOGIN_OK");
        } else {
            sendError("Credenciales inválidas");
        }
    }

    // Maneja el cambio de avatar.
    private void handleSetAvatar(Message msg) {
        if (username == null || msg.getAvatar() == null) {
            sendError("SET_AVATAR inválido");
            return;
        }

        boolean updated = databaseManager.setAvatar(username, msg.getAvatar());

        if (updated) {
            sendSimple("AVATAR_OK");
        } else {
            sendError("No se pudo guardar avatar");
        }
    }

    // Maneja la entrada del jugador a la cola.
    private void handleQueue() {
        if (username == null) {
            sendError("Debe hacer LOGIN o REGISTER primero");
            return;
        }

        matchManager.addPlayerToQueue(this);
    }

    // Maneja la actualización de estado del jugador.
    private void handleState(Message msg) {
        if (session == null || msg.getHp() == null || msg.getScore() == null || msg.getLevel() == null) {
            sendError("STATE inválido o sin sesión");
            return;
        }

        currentHp = msg.getHp();
        currentScore = msg.getScore();
        currentLevel = msg.getLevel();

        Message opponentState = new Message("OPPONENT_STATE");
        opponentState.setHp(currentHp);
        opponentState.setScore(currentScore);
        opponentState.setLevel(currentLevel);

        session.relayState(this, gson.toJson(opponentState));
    }

    // Maneja el fin de la partida.
    private void handleGameOver() {
        if (session == null) {
            sendError("GAME_OVER sin sesión");
            return;
        }

        gameOverSent = true;
        currentHp = 0;
        session.notifyGameOver(this);
    }

    // Cierra la conexión con el cliente.
    private void closeConnection() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.out.println("Error cerrando conexión.");
        }
    }
}