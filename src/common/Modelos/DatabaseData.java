package model;

// Clase que representa la estructura principal del archivo database.json.
// Contiene un arreglo de usuarios que será serializado/deserializado con Gson.
public class DatabaseData {

    // Arreglo de usuarios almacenados en la base de datos.
    private User[] users;

    // Constructor por defecto.
    // Inicializa el arreglo de usuarios vacío para evitar valores null.
    public DatabaseData() {
        this.users = new User[0];
    }

    // Método getter.
    // Devuelve el arreglo completo de usuarios almacenados.
    public User[] getUsers() {
        return users;
    }

    // Método setter.
    // Permite reemplazar el arreglo completo de usuarios (por ejemplo,
    // después de agregar o actualizar información antes de guardar en JSON).
    public void setUsers(User[] users) {
        this.users = users;
    }
}