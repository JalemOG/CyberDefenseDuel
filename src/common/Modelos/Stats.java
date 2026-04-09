package common.Modelos;

public class Stats {
    private int totalScore;
    private int gamesPlayed;
    private int networkXp;
    private int malwareXp;
    private int cryptoXp;

    public Stats() {
        this.totalScore = 0;
        this.gamesPlayed = 0;
        this.networkXp = 0;
        this.malwareXp = 0;
        this.cryptoXp = 0;
    }

    public int getTotalScore() { return totalScore; }
    public void setTotalScore(int totalScore) { this.totalScore = totalScore; }
    public int getGamesPlayed() { return gamesPlayed; }
    public void setGamesPlayed(int gamesPlayed) { this.gamesPlayed = gamesPlayed; }
    public int getNetworkXp() { return networkXp; }
    public void setNetworkXp(int networkXp) { this.networkXp = networkXp; }
    public int getMalwareXp() { return malwareXp; }
    public void setMalwareXp(int malwareXp) { this.malwareXp = malwareXp; }
    public int getCryptoXp() { return cryptoXp; }
    public void setCryptoXp(int cryptoXp) { this.cryptoXp = cryptoXp; }
}