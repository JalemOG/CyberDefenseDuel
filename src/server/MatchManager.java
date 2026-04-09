package server;

import model.Message;
import structures.QueueList;

// Clase encargada de administrar la cola de espera de jugadores.
// Usa una estructura para emparejar jugadores 1 vs 1.
public class MatchManager {

    // Cola de jugadores esperando una partida.
    private final QueueList<ClientHandler> waitingPlayers;

    // Constructor.
    // Inicializa la cola de jugadores en espera.
    public MatchManager() {
        waitingPlayers = new QueueList<>();
    }

    // Agrega un jugador a la cola de espera.
    // Si ya hay al menos dos jugadores, los empareja y crea una nueva sesión.
    public synchronized void addPlayerToQueue(ClientHandler player) {

        // Evita que un mismo jugador entre dos veces a la cola.
        if (player.isInQueue()) {
            Message msg = new Message("ERROR");
            msg.setText("Ya estás en cola");
            player.sendMessage(msg);
            return;
        }

        // Evita que un jugador entre a cola si ya está en una partida.
        if (player.getSession() != null) {
            Message msg = new Message("ERROR");
            msg.setText("Ya estás en una partida");
            player.sendMessage(msg);
            return;
        }

        // Marca al jugador como en cola y lo agrega a la estructura de espera.
        player.setInQueue(true);
        waitingPlayers.enqueue(player);

        // Si hay al menos dos jugadores en espera, se emparejan.
        if (waitingPlayers.size() >= 2) {
            ClientHandler player1 = waitingPlayers.dequeue();
            ClientHandler player2 = waitingPlayers.dequeue();

            // Ambos dejan de estar en cola porque ya fueron emparejados.
            player1.setInQueue(false);
            player2.setInQueue(false);

            // Se crea una nueva sesión de juego entre ambos.
            GameSession session = new GameSession(player1, player2);

            // Se asigna la sesión a cada jugador.
            player1.setSession(session);
            player2.setSession(session);

            // Se guardan como oponentes entre sí.
            player1.setOpponent(player2);
            player2.setOpponent(player1);

            // Se notifica a ambos que la partida fue encontrada.
            player1.sendMessage(Message.simple("MATCH_FOUND"));
            player2.sendMessage(Message.simple("MATCH_FOUND"));

            // Se construye y envía la configuración inicial del juego.
            Message config = new Message("CONFIG");
            config.setHp(100);
            config.setScore(10); // scorePerKill temporal
            config.setLevel(0);  // puedes luego separar estos campos si quieres

            player1.sendMessage(config);
            player2.sendMessage(config);

        } else {
            // Si todavía no hay rival, se le informa al jugador que debe esperar.
            player.sendMessage(Message.simple("WAITING"));
        }
    }
}