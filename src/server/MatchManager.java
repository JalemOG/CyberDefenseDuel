package server;

import common.Modelos.Message;
import common.Estructuras.ListaEnlazada;
import server.Server.ClientHandler;

public class MatchManager {
    
    // Tu propia estructura controlando la fila de espera
    private static final ListaEnlazada<ClientHandler> waitingQueue = new ListaEnlazada<>();

    public static synchronized void joinQueue(ClientHandler player) {
        // 1. Añadir a la fila y avisarle que espere
        waitingQueue.agregar(player);
        player.send(Message.simple("WAITING"));
        System.out.println("🕹️ Jugador " + player.getUsername() + " en cola. Total: " + waitingQueue.getSize());

        // 2. ¿Hay al menos 2 jugadores listos?
        if (waitingQueue.getSize() >= 2) {
            
            // Sacamos a los dos primeros
            ClientHandler p1 = waitingQueue.obtener(0);
            ClientHandler p2 = waitingQueue.obtener(1);

            // Los borramos de la sala de espera
            waitingQueue.eliminar(p1);
            waitingQueue.eliminar(p2);

            System.out.println("⚔️ ¡EMPAREJAMIENTO LISTO: " + p1.getUsername() + " vs " + p2.getUsername() + "!");

            // 3. ¡Creamos la partida y la iniciamos!
            GameSession session = new GameSession(p1, p2);
            session.startMatch();
        }
    }
}