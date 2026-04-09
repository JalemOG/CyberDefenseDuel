package common.Modelos;

public class User {
    private String username;
    private String password;
    private String avatar;
    private Stats stats;

    public User() {
        this.stats = new Stats();
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.avatar = "Captain Firewall";
        this.stats = new Stats();
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public Stats getStats() { return stats; }
    public void setStats(Stats stats) { this.stats = stats; }
}