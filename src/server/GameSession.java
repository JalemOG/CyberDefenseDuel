package server;

import common.Modelos.Message;
import server.Server.ClientHandler;

public class GameSession {
    
    private ClientHandler player1;
    private ClientHandler player2;

    public GameSession(ClientHandler p1, ClientHandler p2) {
        this.player1 = p1;
        this.player2 = p2;

        // Le decimos a cada hilo de red en qué partida están
        p1.setSession(this);
        p2.setSession(this);
    }

    public void startMatch() {
        // Creamos las reglas que enviará el servidor (tu arquitectura original)
        Message startMsg = Message.simple("MATCH_FOUND");
        startMsg.setInitialHp(100);
        startMsg.setLevel(1);
        startMsg.setScorePerKill(10);

        // Disparamos la señal de inicio a ambos a la vez
        player1.send(startMsg);
        player2.send(startMsg);
    }

    // El servidor hace de "espejo" entre los dos jugadores
    public void relayMessage(Message msg, ClientHandler sender) {
        // Descubrimos quién es el oponente
        ClientHandler receiver = (sender == player1) ? player2 : player1;

        if (msg.getType().equals("SYNC_STATS")) {
            // Agarramos tu vida/score y se lo mandamos al enemigo como "OPPONENT_STATS"
            Message oppStats = Message.simple("OPPONENT_STATS");
            oppStats.setHp(msg.getHp());
            oppStats.setScore(msg.getScore());
            receiver.send(oppStats);
        } 
        else if (msg.getType().equals("I_DIED")) {
            // Si tú mueres, le avisamos al otro que ganó
            receiver.send(Message.simple("OPPONENT_DIED"));
        }
    }
}