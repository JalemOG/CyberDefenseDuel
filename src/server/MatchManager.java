package server;

import common.Modelos.Message;
import common.Estructuras.Cola; 

public class MatchManager {

    private final Cola<ClientHandler> waitingPlayers;

    public MatchManager() {
        waitingPlayers = new Cola<>();
    }

    public synchronized void addPlayerToQueue(ClientHandler player) {
        if (player.isInQueue()) {
            Message msg = new Message("ERROR");
            msg.setText("Ya estás en cola");
            player.sendMessage(msg);
            return;
        }

        if (player.getSession() != null) {
            Message msg = new Message("ERROR");
            msg.setText("Ya estás en una partida");
            player.sendMessage(msg);
            return;
        }

        player.setInQueue(true);
        waitingPlayers.encolar(player); 

        if (waitingPlayers.size() >= 2) {
            ClientHandler player1 = waitingPlayers.desencolar(); 
            ClientHandler player2 = waitingPlayers.desencolar();

            player1.setInQueue(false);
            player2.setInQueue(false);

            GameSession session = new GameSession(player1, player2);

            player1.setSession(session);
            player2.setSession(session);

            player1.setOpponent(player2);
            player2.setOpponent(player1);

            player1.sendMessage(Message.simple("MATCH_FOUND"));
            player2.sendMessage(Message.simple("MATCH_FOUND"));

            Message config = new Message("CONFIG");
            config.setInitialHp(100); 
            config.setScorePerKill(10); 
            config.setLevel(0);  

            player1.sendMessage(config);
            player2.sendMessage(config);

        } else {
            player.sendMessage(Message.simple("WAITING"));
        }
    }
}