package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.DatabaseData;
import model.User;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

// Clase encargada de manejar la persistencia de datos en database.json.
// Se encarga de cargar, guardar y modificar la información de usuarios.
public class DatabaseManager {

    // Ruta del archivo JSON donde se almacenan los datos.
    private static final String FILE_PATH = "database.json";

    // Objeto Gson para serializar/deserializar JSON.
    private final Gson gson;

    // Estructura en memoria que contiene los datos del archivo.
    private DatabaseData databaseData;

    // Constructor.
    // Inicializa Gson y carga la base de datos desde el archivo.
    public DatabaseManager() {
        gson = new GsonBuilder().setPrettyPrinting().create();
        loadDatabase();
    }

    // Carga la base de datos desde el archivo JSON.
    // Si el archivo no existe o está corrupto, crea uno nuevo.
    private void loadDatabase() {
        File file = new File(FILE_PATH);

        if (!file.exists()) {
            databaseData = new DatabaseData();
            saveDatabase();
            return;
        }

        try (FileReader reader = new FileReader(file)) {
            databaseData = gson.fromJson(reader, DatabaseData.class);

            // Validación para evitar datos nulos.
            if (databaseData == null || databaseData.getUsers() == null) {
                databaseData = new DatabaseData();
                saveDatabase();
            }

        } catch (IOException e) {
            System.out.println("Error leyendo database.json. Se creará uno nuevo.");
            databaseData = new DatabaseData();
            saveDatabase();
        }
    }

    // Guarda el estado actual de la base de datos en el archivo JSON.
    // Se usa synchronized para evitar conflictos entre hilos.
    public synchronized void saveDatabase() {
        try (FileWriter writer = new FileWriter(FILE_PATH)) {
            gson.toJson(databaseData, writer);
        } catch (IOException e) {
            System.out.println("Error guardando database.json: " + e.getMessage());
        }
    }

    // Registra un nuevo usuario en la base de datos.
    // Retorna false si el usuario ya existe.
    public synchronized boolean registerUser(String username, String password) {
        if (findUser(username) != null) {
            return false;
        }

        User newUser = new User(username, password);

        // Se crea un nuevo arreglo (no se usan colecciones de Java).
        User[] currentUsers = databaseData.getUsers();
        User[] newUsers = new User[currentUsers.length + 1];

        // Copia manual del arreglo actual.
        for (int i = 0; i < currentUsers.length; i++) {
            newUsers[i] = currentUsers[i];
        }

        // Se agrega el nuevo usuario al final.
        newUsers[currentUsers.length] = newUser;
        databaseData.setUsers(newUsers);
        saveDatabase();

        return true;
    }

    // Valida las credenciales de un usuario.
    public synchronized boolean loginUser(String username, String password) {
        User user = findUser(username);
        if (user == null) {
            return false;
        }
        return user.getPassword().equals(password);
    }

    // Busca un usuario por su nombre.
    // Retorna null si no existe.
    public synchronized User findUser(String username) {
        User[] users = databaseData.getUsers();

        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }

        return null;
    }

    // Actualiza el avatar de un usuario.
    public synchronized boolean setAvatar(String username, String avatar) {
        User user = findUser(username);
        if (user == null) {
            return false;
        }

        user.setAvatar(avatar);
        saveDatabase();
        return true;
    }

    // Guarda los resultados de una partida.
    // Actualiza estadísticas acumuladas del usuario.
    public synchronized boolean addMatchResult(String username, int score, int networkXp, int malwareXp, int cryptoXp) {
        User user = findUser(username);
        if (user == null) {
            return false;
        }

        // Actualización de estadísticas.
        user.getStats().setGamesPlayed(user.getStats().getGamesPlayed() + 1);
        user.getStats().setTotalScore(user.getStats().getTotalScore() + score);
        user.getStats().setNetworkXp(user.getStats().getNetworkXp() + networkXp);
        user.getStats().setMalwareXp(user.getStats().getMalwareXp() + malwareXp);
        user.getStats().setCryptoXp(user.getStats().getCryptoXp() + cryptoXp);

        saveDatabase();
        return true;
    }
}