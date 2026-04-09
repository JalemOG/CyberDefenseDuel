package client;

import com.google.gson.Gson;
import common.Modelos.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {

    private static final String HOST = "localhost";
    private static final int PORT = 5000;

    private static volatile boolean authenticated = false;
    private static volatile boolean inMatch = false;
    private static volatile boolean waitingForMatch = false;
    private static volatile boolean showLobby = false;
    private static volatile boolean waitingAuthResponse = false;

    public static void main(String[] args) {
        Gson gson = new Gson();

        try {
            Socket socket = new Socket(HOST, PORT);
            System.out.println("Conectado al servidor en " + HOST + ":" + PORT);

            BufferedReader serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter serverOut = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));

            Thread listener = new Thread(() -> {
                try {
                    String message;
                    while ((message = serverIn.readLine()) != null) {
                        Message msg = gson.fromJson(message, Message.class);

                        if (msg.getType() == null) continue;

                        switch (msg.getType()) {
                            case "WELCOME": System.out.println("Servidor: " + msg.getText()); break;
                            case "LOGIN_OK":
                                authenticated = true; waitingAuthResponse = false; showLobby = true;
                                System.out.println("Servidor: Login exitoso"); break;
                            case "REGISTER_OK":
                                authenticated = true; waitingAuthResponse = false; showLobby = true;
                                System.out.println("Servidor: Registro exitoso"); break;
                            case "MATCH_FOUND":
                                inMatch = true; waitingForMatch = false; showLobby = false;
                                System.out.println("Servidor: Partida encontrada"); break;
                            case "WAITING":
                                waitingForMatch = true; showLobby = false;
                                System.out.println("Servidor: Esperando al otro jugador..."); break;
                            case "CONFIG":
                                System.out.println("=== CONFIGURACIÓN DEL JUEGO ===");
                                System.out.println("HP inicial: " + msg.getInitialHp());
                                System.out.println("Score por kill: " + msg.getScorePerKill());
                                System.out.println("Nivel: " + msg.getLevel());
                                System.out.println("================================");
                                break;
                            case "AVATAR_OK":
                                System.out.println("Servidor: Avatar guardado"); showLobby = true; break;
                            case "OPPONENT_STATE":
                                System.out.println("Oponente -> HP: " + msg.getHp() + " | Score: " + msg.getScore() + " | Level: " + msg.getLevel()); break;
                            case "OPPONENT_GAME_OVER":
                                System.out.println("Servidor: El oponente perdió"); break;
                            case "RESULT_SAVED":
                                System.out.println("Servidor: Resultado guardado"); break;
                            case "SESSION_CLOSED":
                                inMatch = false; waitingForMatch = false; showLobby = true;
                                System.out.println("Servidor: Sesión finalizada"); break;
                            case "ERROR":
                                waitingAuthResponse = false;
                                System.out.println("Error: " + msg.getText());
                                if (!authenticated) System.out.println("Vuelve a intentarlo.");
                                else if (!inMatch && !waitingForMatch) showLobby = true;
                                break;
                            default:
                                System.out.println("Servidor JSON: " + message); break;
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Conexión cerrada con el servidor.");
                }
            });

            listener.start();
            Thread.sleep(300);

            while (true) {
                if (!authenticated) {
                    if (!waitingAuthResponse) showInitialMenu(keyboard, serverOut, gson);
                    else Thread.sleep(200);
                    continue;
                }

                if (showLobby && !inMatch && !waitingForMatch) {
                    showLobbyMenu(keyboard, serverOut, gson);
                    Thread.sleep(200);
                    continue;
                }

                if (inMatch) {
                    String input = keyboard.readLine();
                    if (input == null) break;

                    Message msg = parseMatchCommand(input);
                    if (msg != null) serverOut.println(gson.toJson(msg));
                    else System.out.println("Comando inválido. Usa STATE|hp|score|level o GAME_OVER");
                } else {
                    Thread.sleep(200);
                }
            }

        } catch (IOException | InterruptedException e) {
            System.out.println("Error del cliente: " + e.getMessage());
        }
    }

    private static void showInitialMenu(BufferedReader keyboard, PrintWriter serverOut, Gson gson) throws IOException {
        System.out.println("\n=== MENÚ INICIAL ===\n1. Registrar jugador\n2. Iniciar sesión\nSeleccione una opción: ");
        String option = keyboard.readLine();

        if ("1".equals(option) || "2".equals(option)) {
            System.out.print("Username: "); String username = keyboard.readLine();
            System.out.print("Password: "); String password = keyboard.readLine();

            Message msg = new Message("1".equals(option) ? "REGISTER" : "LOGIN");
            msg.setUsername(username); msg.setPassword(password);

            waitingAuthResponse = true;
            serverOut.println(gson.toJson(msg));
        } else {
            System.out.println("Opción inválida.");
        }
    }

    private static void showLobbyMenu(BufferedReader keyboard, PrintWriter serverOut, Gson gson) throws IOException {
        showLobby = false;
        System.out.println("\n=== LOBBY ===\n1. Buscar partida\n2. Cambiar avatar\n3. Salir\nSeleccione una opción: ");
        String option = keyboard.readLine();

        switch (option) {
            case "1": serverOut.println(gson.toJson(Message.simple("QUEUE"))); break;
            case "2":
                System.out.print("Nombre del avatar: "); String avatar = keyboard.readLine();
                Message avatarMsg = new Message("SET_AVATAR"); avatarMsg.setAvatar(avatar);
                serverOut.println(gson.toJson(avatarMsg)); break;
            case "3": System.out.println("Cliente cerrado."); System.exit(0); break;
            default: System.out.println("Opción inválida."); showLobby = true; break;
        }
    }

    private static Message parseMatchCommand(String input) {
        String[] parts = input.split("\\|");
        switch (parts[0].toUpperCase()) {
            case "STATE":
                if (parts.length >= 4) {
                    try {
                        Message msg = new Message("STATE");
                        msg.setHp(Integer.parseInt(parts[1]));
                        msg.setScore(Integer.parseInt(parts[2]));
                        msg.setLevel(Integer.parseInt(parts[3]));
                        return msg;
                    } catch (NumberFormatException e) {
                        return null; 
                    }
                }
                return null;
            case "GAME_OVER":
                return Message.simple("GAME_OVER");
            default:
                return null;
        }
    }
}