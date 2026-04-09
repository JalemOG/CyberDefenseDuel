package common.Modelos;

public class DatabaseData {
    private User[] users;

    public DatabaseData() {
        this.users = new User[0];
    }

    public User[] getUsers() { return users; }
    public void setUsers(User[] users) { this.users = users; }
}