package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import common.Modelos.DatabaseData;
import common.Modelos.User;
import common.Estructuras.ListaEnlazada;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class DatabaseManager {

    private static final String FILE_PATH = "database.json";
    private final Gson gson;
    private DatabaseData databaseData;
    private ListaEnlazada<User> usuariosEnMemoria; 

    public DatabaseManager() {
        gson = new GsonBuilder().setPrettyPrinting().create();
        loadDatabase();
    }

    private void loadDatabase() {
        File file = new File(FILE_PATH);
        usuariosEnMemoria = new ListaEnlazada<>();

        if (!file.exists()) {
            databaseData = new DatabaseData();
            saveDatabase();
            return;
        }

        try (FileReader reader = new FileReader(file)) {
            databaseData = gson.fromJson(reader, DatabaseData.class);

            if (databaseData == null || databaseData.getUsers() == null) {
                databaseData = new DatabaseData();
            } else {
                for (User u : databaseData.getUsers()) {
                    usuariosEnMemoria.agregar(u);
                }
            }
        } catch (IOException e) {
            System.out.println("Error leyendo database.json. Se creará uno nuevo.");
            databaseData = new DatabaseData();
            saveDatabase();
        }
    }

    public synchronized void saveDatabase() {
        // En tu UML vi que el método para obtener el tamaño es getSize(), y para el índice obtener(indice)
        User[] arrayParaGuardar = new User[usuariosEnMemoria.getSize()];
        for (int i = 0; i < usuariosEnMemoria.getSize(); i++) {
            arrayParaGuardar[i] = usuariosEnMemoria.obtener(i); 
        }
        databaseData.setUsers(arrayParaGuardar);

        try (FileWriter writer = new FileWriter(FILE_PATH)) {
            gson.toJson(databaseData, writer);
        } catch (IOException e) {
            System.out.println("Error guardando database.json: " + e.getMessage());
        }
    }

    public synchronized boolean registerUser(String username, String password) {
        if (findUser(username) != null) {
            return false;
        }

        User newUser = new User(username, password);
        usuariosEnMemoria.agregar(newUser); 
        saveDatabase();
        return true;
    }

    public synchronized boolean loginUser(String username, String password) {
        User user = findUser(username);
        if (user == null) {
            return false;
        }
        return user.getPassword().equals(password);
    }

    public synchronized User findUser(String username) {
        for (int i = 0; i < usuariosEnMemoria.getSize(); i++) {
            User user = usuariosEnMemoria.obtener(i);
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }

    public synchronized boolean setAvatar(String username, String avatar) {
        User user = findUser(username);
        if (user == null) {
            return false;
        }

        user.setAvatar(avatar);
        saveDatabase();
        return true;
    }

    public synchronized boolean addMatchResult(String username, int score, int networkXp, int malwareXp, int cryptoXp) {
        User user = findUser(username);
        if (user == null) {
            return false;
        }

        user.getStats().setGamesPlayed(user.getStats().getGamesPlayed() + 1);
        user.getStats().setTotalScore(user.getStats().getTotalScore() + score);
        user.getStats().setNetworkXp(user.getStats().getNetworkXp() + networkXp);
        user.getStats().setMalwareXp(user.getStats().getMalwareXp() + malwareXp);
        user.getStats().setCryptoXp(user.getStats().getCryptoXp() + cryptoXp);

        saveDatabase();
        return true;
    }
}