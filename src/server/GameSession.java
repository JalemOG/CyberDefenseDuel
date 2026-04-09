package server;

import common.Modelos.Message;

public class GameSession {
    private final ClientHandler player1;
    private final ClientHandler player2;
    private boolean resultsSaved = false;
    private boolean sessionClosed = false;

    public GameSession(ClientHandler player1, ClientHandler player2) {
        this.player1 = player1;
        this.player2 = player2;
    }

    public void relayState(ClientHandler sender, String jsonMessage) {
        if (sessionClosed) {
            return;
        }

        ClientHandler receiver = (sender == player1) ? player2 : player1;
        if (receiver != null) {
            receiver.sendMessage(fromJsonString(jsonMessage));
        }
    }

    private Message fromJsonString(String json) {
        return new com.google.gson.Gson().fromJson(json, Message.class);
    }

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

    private int calculateNetworkXp(ClientHandler player) {
        return player.getCurrentLevel() * 2;
    }

    private int calculateMalwareXp(ClientHandler player) {
        return player.getCurrentScore() / 10;
    }

    private int calculateCryptoXp(ClientHandler player) {
        return Math.max(1, player.getCurrentScore() / 20);
    }
}