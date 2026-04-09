package client;

import com.google.gson.Gson;
import model.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {

    // Dirección y puerto del servidor al que se conectará el cliente.
    private static final String HOST = "localhost";
    private static final int PORT = 5000;

    // Banderas de estado del cliente para controlar el flujo del menú y la partida.
    private static volatile boolean authenticated = false;
    private static volatile boolean inMatch = false;
    private static volatile boolean waitingForMatch = false;
    private static volatile boolean showLobby = false;
    private static volatile boolean waitingAuthResponse = false;

    // Método principal del cliente.
    // Se encarga de abrir la conexión con el servidor, iniciar el hilo de escucha
    // y controlar el flujo entre menú inicial, lobby y comandos durante la partida.
    public static void main(String[] args) {
        Gson gson = new Gson();

        try {
            // Se crea la conexión del cliente con el servidor.
            Socket socket = new Socket(HOST, PORT);
            System.out.println("Conectado al servidor en " + HOST + ":" + PORT);

            // Canal para leer mensajes que llegan desde el servidor.
            BufferedReader serverIn = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
            );

            // Canal para enviar mensajes al servidor.
            PrintWriter serverOut = new PrintWriter(
                    socket.getOutputStream(), true
            );

            // Canal para leer texto escrito por el usuario en consola.
            BufferedReader keyboard = new BufferedReader(
                    new InputStreamReader(System.in)
            );

            // Hilo encargado de escuchar continuamente al servidor sin bloquear
            // la lectura del teclado en el hilo principal.
            Thread listener = new Thread(() -> {
                try {
                    String message;
                    while ((message = serverIn.readLine()) != null) {
                        // Convierte cada mensaje JSON recibido a un objeto Message.
                        Message msg = gson.fromJson(message, Message.class);

                        if (msg.getType() == null) {
                            continue;
                        }

                        // Según el tipo de mensaje recibido, actualiza el estado local
                        // del cliente y muestra información en consola.
                        switch (msg.getType()) {
                            case "WELCOME":
                                System.out.println("Servidor: " + msg.getText());
                                break;

                            case "LOGIN_OK":
                                authenticated = true;
                                waitingAuthResponse = false;
                                showLobby = true;
                                System.out.println("Servidor: Login exitoso");
                                break;

                            case "REGISTER_OK":
                                authenticated = true;
                                waitingAuthResponse = false;
                                showLobby = true;
                                System.out.println("Servidor: Registro exitoso");
                                break;

                            case "MATCH_FOUND":
                                inMatch = true;
                                waitingForMatch = false;
                                showLobby = false;
                                System.out.println("Servidor: Partida encontrada");
                                break;

                            case "WAITING":
                                waitingForMatch = true;
                                showLobby = false;
                                System.out.println("Servidor: Esperando al otro jugador...");
                                break;

                            case "CONFIG":
                                System.out.println("=== CONFIGURACIÓN DEL JUEGO ===");
                                System.out.println("HP inicial: " + msg.getInitialHp());
                                System.out.println("Spawn base: " + msg.getBaseSpawnRate());
                                System.out.println("Velocidad base ataques: " + msg.getBaseAttackSpeed());
                                System.out.println("Score por kill: " + msg.getScorePerKill());
                                System.out.println("Cada " + msg.getDifficultyStepScore() + " puntos sube dificultad");
                                System.out.println("Multiplicador spawn por nivel: " + msg.getSpawnMultiplierPerLevel());
                                System.out.println("Aumento velocidad por nivel: " + msg.getSpeedAddPerLevel());
                                System.out.println("Daño DDOS: " + msg.getDamageDDOS());
                                System.out.println("Daño MALWARE: " + msg.getDamageMALWARE());
                                System.out.println("Daño CRED: " + msg.getDamageCRED());
                                System.out.println("================================");
                                System.out.println("Ya puedes usar durante la partida:");
                                System.out.println("STATE|80|150|2");
                                System.out.println("GAME_OVER");
                                break;

                            case "AVATAR_OK":
                                System.out.println("Servidor: Avatar guardado correctamente");
                                showLobby = true;
                                break;

                            case "OPPONENT_STATE":
                                System.out.println("Oponente -> HP: " + msg.getHp()
                                        + " | Score: " + msg.getScore()
                                        + " | Level: " + msg.getLevel());
                                break;

                            case "OPPONENT_GAME_OVER":
                                System.out.println("Servidor: El oponente perdió");
                                break;

                            case "RESULT_SAVED":
                                System.out.println("Servidor: Resultado guardado");
                                break;

                            case "SESSION_CLOSED":
                                inMatch = false;
                                waitingForMatch = false;
                                showLobby = true;
                                System.out.println("Servidor: Sesión finalizada");
                                break;

                            case "ERROR":
                                waitingAuthResponse = false;
                                System.out.println("Error: " + msg.getText());

                                if (!authenticated) {
                                    System.out.println("Vuelve a intentarlo.");
                                } else if (!inMatch && !waitingForMatch) {
                                    showLobby = true;
                                }
                                break;

                            default:
                                System.out.println("Servidor JSON: " + message);
                                break;
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Conexión cerrada con el servidor.");
                }
            });

            // Se inicia el hilo que escucha mensajes del servidor.
            listener.start();

            // Pausa breve para dar tiempo a que llegue el mensaje inicial del servidor.
            Thread.sleep(300);

            // Bucle principal del cliente.
            // Decide qué menú mostrar o qué comandos aceptar según el estado actual.
            while (true) {
                if (!authenticated) {
                    if (!waitingAuthResponse) {
                        showInitialMenu(keyboard, serverOut, gson);
                    } else {
                        Thread.sleep(200);
                    }
                    continue;
                }

                if (showLobby && !inMatch && !waitingForMatch) {
                    showLobbyMenu(keyboard, serverOut, gson);
                    Thread.sleep(200);
                    continue;
                }

                if (inMatch) {
                    String input = keyboard.readLine();
                    if (input == null) {
                        break;
                    }

                    // Convierte el comando escrito durante la partida en un mensaje
                    // que luego será enviado al servidor en formato JSON.
                    Message msg = parseMatchCommand(input);
                    if (msg != null) {
                        serverOut.println(gson.toJson(msg));
                    } else {
                        System.out.println("Comando inválido durante la partida");
                        System.out.println("Usa:");
                        System.out.println("STATE|hp|score|level");
                        System.out.println("GAME_OVER");
                    }
                } else {
                    Thread.sleep(200);
                }
            }

        } catch (IOException e) {
            System.out.println("Error del cliente: " + e.getMessage());
        } catch (InterruptedException e) {
            System.out.println("Interrupción inesperada.");
        }
    }

    // Muestra el menú inicial para registrar o iniciar sesión.
    // También construye y envía el mensaje correspondiente al servidor.
    private static void showInitialMenu(BufferedReader keyboard, PrintWriter serverOut, Gson gson) throws IOException {
        System.out.println();
        System.out.println("=== MENÚ INICIAL ===");
        System.out.println("1. Registrar jugador");
        System.out.println("2. Iniciar sesión");
        System.out.print("Seleccione una opción: ");

        String option = keyboard.readLine();

        if ("1".equals(option)) {
            System.out.print("Ingrese nombre de usuario: ");
            String username = keyboard.readLine();

            System.out.print("Ingrese contraseña: ");
            String password = keyboard.readLine();

            Message msg = new Message("REGISTER");
            msg.setUsername(username);
            msg.setPassword(password);

            waitingAuthResponse = true;
            serverOut.println(gson.toJson(msg));

        } else if ("2".equals(option)) {
            System.out.print("Ingrese nombre de usuario: ");
            String username = keyboard.readLine();

            System.out.print("Ingrese contraseña: ");
            String password = keyboard.readLine();

            Message msg = new Message("LOGIN");
            msg.setUsername(username);
            msg.setPassword(password);

            waitingAuthResponse = true;
            serverOut.println(gson.toJson(msg));

        } else {
            System.out.println("Opción inválida. Intente de nuevo.");
        }
    }

    // Muestra el lobby una vez autenticado el usuario.
    // Desde aquí se puede buscar partida, cambiar avatar o salir del cliente.
    private static void showLobbyMenu(BufferedReader keyboard, PrintWriter serverOut, Gson gson) throws IOException {
        showLobby = false;

        System.out.println();
        System.out.println("=== LOBBY ===");
        System.out.println("1. Buscar partida");
        System.out.println("2. Cambiar avatar");
        System.out.println("3. Salir");
        System.out.print("Seleccione una opción: ");

        String option = keyboard.readLine();

        switch (option) {
            case "1":
                serverOut.println(gson.toJson(Message.simple("QUEUE")));
                break;

            case "2":
                System.out.print("Ingrese el nombre del avatar: ");
                String avatar = keyboard.readLine();

                Message avatarMsg = new Message("SET_AVATAR");
                avatarMsg.setAvatar(avatar);

                serverOut.println(gson.toJson(avatarMsg));
                break;

            case "3":
                System.out.println("Cliente cerrado.");
                System.exit(0);
                break;

            default:
                System.out.println("Opción inválida.");
                showLobby = true;
                break;
        }
    }

    // Convierte los comandos escritos durante la partida a objetos Message.
    // Actualmente permite mandar estado del jugador o indicar fin de partida.
    private static Message parseMatchCommand(String input) {
        String[] parts = input.split("\\|");

        switch (parts[0].toUpperCase()) {
            case "STATE":
                if (parts.length >= 4) {
                    Message msg = new Message("STATE");
                    msg.setHp(Integer.parseInt(parts[1]));
                    msg.setScore(Integer.parseInt(parts[2]));
                    msg.setLevel(Integer.parseInt(parts[3]));
                    return msg;
                }
                return null;

            case "GAME_OVER":
                return Message.simple("GAME_OVER");

            default:
                return null;
        }
    }
}