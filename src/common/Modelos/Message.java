package common.Modelos;

public class Message {
    private String type;
    private String username;
    private String password;
    private String avatar;
    private Integer hp;
    private Integer score;
    private Integer level;
    private String text;
    private Integer initialHp;
    private Double baseSpawnRate;
    private Double baseAttackSpeed;
    private Integer scorePerKill;
    private Integer difficultyStepScore;
    private Double spawnMultiplierPerLevel;
    private Double speedAddPerLevel;
    private Integer damageDDOS;
    private Integer damageMALWARE;
    private Integer damageCRED;

    public Message() {}
    public Message(String type) { this.type = type; }
    public static Message simple(String type) { return new Message(type); }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public Integer getHp() { return hp; }
    public void setHp(Integer hp) { this.hp = hp; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public Integer getInitialHp() { return initialHp; }
    public void setInitialHp(Integer initialHp) { this.initialHp = initialHp; }
    public Double getBaseSpawnRate() { return baseSpawnRate; }
    public void setBaseSpawnRate(Double baseSpawnRate) { this.baseSpawnRate = baseSpawnRate; }
    public Double getBaseAttackSpeed() { return baseAttackSpeed; }
    public void setBaseAttackSpeed(Double baseAttackSpeed) { this.baseAttackSpeed = baseAttackSpeed; }
    public Integer getScorePerKill() { return scorePerKill; }
    public void setScorePerKill(Integer scorePerKill) { this.scorePerKill = scorePerKill; }
    public Integer getDifficultyStepScore() { return difficultyStepScore; }
    public void setDifficultyStepScore(Integer difficultyStepScore) { this.difficultyStepScore = difficultyStepScore; }
    public Double getSpawnMultiplierPerLevel() { return spawnMultiplierPerLevel; }
    public void setSpawnMultiplierPerLevel(Double spawnMultiplierPerLevel) { this.spawnMultiplierPerLevel = spawnMultiplierPerLevel; }
    public Double getSpeedAddPerLevel() { return speedAddPerLevel; }
    public void setSpeedAddPerLevel(Double speedAddPerLevel) { this.speedAddPerLevel = speedAddPerLevel; }
    public Integer getDamageDDOS() { return damageDDOS; }
    public void setDamageDDOS(Integer damageDDOS) { this.damageDDOS = damageDDOS; }
    public Integer getDamageMALWARE() { return damageMALWARE; }
    public void setDamageMALWARE(Integer damageMALWARE) { this.damageMALWARE = damageMALWARE; }
    public Integer getDamageCRED() { return damageCRED; }
    public void setDamageCRED(Integer damageCRED) { this.damageCRED = damageCRED; }
}