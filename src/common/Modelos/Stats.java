package model;

// Clase que representa las estadísticas acumuladas de un jugador.
// Se usa principalmente para guardar y cargar información desde database.json.
public class Stats {

    // Puntaje total acumulado por el jugador.
    private int totalScore;

    // Cantidad de partidas jugadas.
    private int gamesPlayed;

    // Experiencia en categoría de redes.
    private int networkXp;

    // Experiencia en categoría de malware.
    private int malwareXp;

    // Experiencia en categoría de criptografía.
    private int cryptoXp;

    // Constructor por defecto.
    // Inicializa todas las estadísticas en cero para evitar valores basura.
    public Stats() {
        this.totalScore = 0;
        this.gamesPlayed = 0;
        this.networkXp = 0;
        this.malwareXp = 0;
        this.cryptoXp = 0;
    }

    // Devuelve el puntaje total acumulado.
    public int getTotalScore() {
        return totalScore;
    }

    // Permite actualizar el puntaje total.
    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    // Devuelve la cantidad de partidas jugadas.
    public int getGamesPlayed() {
        return gamesPlayed;
    }

    // Permite actualizar la cantidad de partidas jugadas.
    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    // Devuelve la experiencia en redes.
    public int getNetworkXp() {
        return networkXp;
    }

    // Permite actualizar la experiencia en redes.
    public void setNetworkXp(int networkXp) {
        this.networkXp = networkXp;
    }

    // Devuelve la experiencia en malware.
    public int getMalwareXp() {
        return malwareXp;
    }

    // Permite actualizar la experiencia en malware.
    public void setMalwareXp(int malwareXp) {
        this.malwareXp = malwareXp;
    }

    // Devuelve la experiencia en criptografía.
    public int getCryptoXp() {
        return cryptoXp;
    }

    // Permite actualizar la experiencia en criptografía.
    public void setCryptoXp(int cryptoXp) {
        this.cryptoXp = cryptoXp;
    }
}