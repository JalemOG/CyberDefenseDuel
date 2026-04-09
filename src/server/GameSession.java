package server;

import model.Message;

// Clase que representa una sesión de juego entre dos jugadores.
// Se encarga de reenviar estados, manejar el fin de partida,
// guardar resultados y cerrar la sesión.
public class GameSession {

    // Referencias a los dos jugadores participantes de la sesión.
    private final ClientHandler player1;
    private final ClientHandler player2;

    // Banderas de control para evitar guardar resultados o cerrar la sesión más de una vez.
    private boolean resultsSaved = false;
    private boolean sessionClosed = false;

    // Constructor de la sesión.
    // Recibe los dos jugadores emparejados.
    public GameSession(ClientHandler player1, ClientHandler player2) {
        this.player1 = player1;
        this.player2 = player2;
    }

    // Reenvía el estado de un jugador al oponente.
    // Recibe un mensaje JSON, lo convierte a Message y lo manda al rival.
    public void relayState(ClientHandler sender, String jsonMessage) {
        if (sessionClosed) {
            return;
        }

        ClientHandler receiver = (sender == player1) ? player2 : player1;
        if (receiver != null) {
            Message msg = new Message();
            receiver.sendMessage(fromJsonString(jsonMessage));
        }
    }

    // Convierte un String JSON a un objeto Message.
    private Message fromJsonString(String json) {
        return new com.google.gson.Gson().fromJson(json, Message.class);
    }

    // Maneja el evento de fin de partida cuando uno de los jugadores pierde.
    // Notifica al oponente, guarda resultados y cierra la sesión.
    public synchronized void notifyGameOver(ClientHandler sender) {
        if (sessionClosed) {
            return;
        }

        ClientHandler receiver = (sender == player1) ? player2 : player1;

        if (receiver != null) {
            receiver.sendMessage(Message.simple("OPPONENT_GAME_OVER"));
        }

        saveResultsIfNeeded();
        closeSession();
    }

    // Guarda los resultados de ambos jugadores si aún no se han guardado.
    // Calcula XP y actualiza la base de datos.
    private synchronized void saveResultsIfNeeded() {
        if (resultsSaved) {
            return;
        }

        DatabaseManager db = player1.getDatabaseManager();

        int p1Score = player1.getCurrentScore();
        int p2Score = player2.getCurrentScore();

        int p1NetworkXp = calculateNetworkXp(player1);
        int p1MalwareXp = calculateMalwareXp(player1);
        int p1CryptoXp = calculateCryptoXp(player1);

        int p2NetworkXp = calculateNetworkXp(player2);
        int p2MalwareXp = calculateMalwareXp(player2);
        int p2CryptoXp = calculateCryptoXp(player2);

        boolean saved1 = db.addMatchResult(player1.getUsername(), p1Score, p1NetworkXp, p1MalwareXp, p1CryptoXp);
        boolean saved2 = db.addMatchResult(player2.getUsername(), p2Score, p2NetworkXp, p2MalwareXp, p2CryptoXp);

        if (saved1 && saved2) {
            resultsSaved = true;
            player1.sendMessage(Message.simple("RESULT_SAVED"));
            player2.sendMessage(Message.simple("RESULT_SAVED"));
        }
    }

    // Cierra la sesión de juego si aún no se ha cerrado.
    // Notifica a ambos jugadores y reinicia su estado para futuras partidas.
    private synchronized void closeSession() {
        if (sessionClosed) {
            return;
        }

        sessionClosed = true;

        player1.sendMessage(Message.simple("SESSION_CLOSED"));
        player2.sendMessage(Message.simple("SESSION_CLOSED"));

        player1.resetForNewMatch();
        player2.resetForNewMatch();
    }

    // Calcula la experiencia de red en función del nivel actual del jugador.
    private int calculateNetworkXp(ClientHandler player) {
        return player.getCurrentLevel() * 2;
    }

    // Calcula la experiencia de malware en función del score actual.
    private int calculateMalwareXp(ClientHandler player) {
        return player.getCurrentScore() / 10;
    }

    // Calcula la experiencia de criptografía en función del score actual.
    // Asegura que al menos se gane 1 punto.
    private int calculateCryptoXp(ClientHandler player) {
        return Math.max(1, player.getCurrentScore() / 20);
    }
}