package server;

import com.google.gson.Gson;
import common.Modelos.Message;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler extends Thread {

    private final Socket socket;
    private final MatchManager matchManager;
    private final DatabaseManager databaseManager;
    private final Gson gson = new Gson();

    private BufferedReader in;
    private PrintWriter out;

    private String username;
    private ClientHandler opponent;
    private GameSession session;
    private boolean inQueue = false;

    private int currentHp = 100;
    private int currentScore = 0;
    private int currentLevel = 0;
    private boolean gameOverSent = false;

    public ClientHandler(Socket socket, MatchManager matchManager, DatabaseManager databaseManager) {
        this.socket = socket;
        this.matchManager = matchManager;
        this.databaseManager = databaseManager;
    }

    public String getUsername() { return username == null ? "JugadorSinNombre" : username; }
    public void setOpponent(ClientHandler opponent) { this.opponent = opponent; }
    public ClientHandler getOpponent() { return opponent; }
    public void setSession(GameSession session) { this.session = session; }
    public GameSession getSession() { return session; }
    public boolean isInQueue() { return inQueue; }
    public void setInQueue(boolean inQueue) { this.inQueue = inQueue; }
    public int getCurrentHp() { return currentHp; }
    public int getCurrentScore() { return currentScore; }
    public int getCurrentLevel() { return currentLevel; }
    public boolean hasSentGameOver() { return gameOverSent; }
    public DatabaseManager getDatabaseManager() { return databaseManager; }

    public void resetForNewMatch() {
        this.opponent = null;
        this.session = null;
        this.inQueue = false;
        this.currentHp = 100;
        this.currentScore = 0;
        this.currentLevel = 0;
        this.gameOverSent = false;
    }

    public void sendMessage(Message message) {
        if (out != null) {
            out.println(gson.toJson(message));
        }
    }

    private void sendSimple(String type) {
        sendMessage(Message.simple(type));
    }

    private void sendError(String text) {
        Message msg = new Message("ERROR");
        msg.setText(text);
        sendMessage(msg);
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            Message welcome = new Message("WELCOME");
            welcome.setText("Bienvenido al servidor");
            sendMessage(welcome);

            String raw;
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

    private void handleMessage(String raw) {
        try {
            Message msg = gson.fromJson(raw, Message.class);

            if (msg == null || msg.getType() == null) {
                sendError("Mensaje JSON inválido");
                return;
            }

            switch (msg.getType()) {
                case "PING": sendSimple("PONG"); break;
                case "REGISTER": handleRegister(msg); break;
                case "LOGIN": handleLogin(msg); break;
                case "SET_AVATAR": handleSetAvatar(msg); break;
                case "QUEUE": handleQueue(); break;
                case "STATE": handleState(msg); break;
                case "GAME_OVER": handleGameOver(); break;
                default: sendError("Tipo de mensaje no reconocido"); break;
            }

        } catch (Exception e) {
            sendError("No se pudo procesar el JSON");
        }
    }

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

    private void handleQueue() {
        if (username == null) {
            sendError("Debe hacer LOGIN o REGISTER primero");
            return;
        }
        matchManager.addPlayerToQueue(this);
    }

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

    private void handleGameOver() {
        if (session == null) {
            sendError("GAME_OVER sin sesión");
            return;
        }
        gameOverSent = true;
        currentHp = 0;
        session.notifyGameOver(this);
    }

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