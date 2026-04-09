package model;

// Clase que representa todos los mensajes que se envían entre cliente y servidor.
// Se usa para serializar y deserializar JSON usando Gson.
public class Message {

    // Tipo de mensaje (ej: LOGIN, REGISTER, CONFIG, STATE, etc.)
    private String type;

    // Datos de autenticación del usuario.
    private String username;
    private String password;

    // Avatar seleccionado por el usuario.
    private String avatar;

    // Datos del estado del jugador durante la partida.
    private Integer hp;
    private Integer score;
    private Integer level;

    // Texto genérico para mensajes simples (ej: errores, bienvenida).
    private String text;

    // Configuración inicial del juego enviada por el servidor.
    private Integer initialHp;
    private Double baseSpawnRate;
    private Double baseAttackSpeed;
    private Integer scorePerKill;
    private Integer difficultyStepScore;
    private Double spawnMultiplierPerLevel;
    private Double speedAddPerLevel;

    // Daños asociados a distintos tipos de ataques.
    private Integer damageDDOS;
    private Integer damageMALWARE;
    private Integer damageCRED;

    // Constructor vacío requerido para Gson (deserialización).
    public Message() {
    }

    // Constructor que permite crear un mensaje indicando su tipo.
    public Message(String type) {
        this.type = type;
    }

    // Método auxiliar para crear mensajes simples solo con tipo.
    public static Message simple(String type) {
        return new Message(type);
    }

    // Getter del tipo de mensaje.
    public String getType() {
        return type;
    }

    // Setter del tipo de mensaje.
    public void setType(String type) {
        this.type = type;
    }

    // Getter del nombre de usuario.
    public String getUsername() {
        return username;
    }

    // Setter del nombre de usuario.
    public void setUsername(String username) {
        this.username = username;
    }

    // Getter de la contraseña.
    public String getPassword() {
        return password;
    }

    // Setter de la contraseña.
    public void setPassword(String password) {
        this.password = password;
    }

    // Getter del avatar.
    public String getAvatar() {
        return avatar;
    }

    // Setter del avatar.
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    // Getter del HP del jugador.
    public Integer getHp() {
        return hp;
    }

    // Setter del HP del jugador.
    public void setHp(Integer hp) {
        this.hp = hp;
    }

    // Getter del score del jugador.
    public Integer getScore() {
        return score;
    }

    // Setter del score del jugador.
    public void setScore(Integer score) {
        this.score = score;
    }

    // Getter del nivel del jugador.
    public Integer getLevel() {
        return level;
    }

    // Setter del nivel del jugador.
    public void setLevel(Integer level) {
        this.level = level;
    }

    // Getter de texto genérico.
    public String getText() {
        return text;
    }

    // Setter de texto genérico.
    public void setText(String text) {
        this.text = text;
    }

    // Getter de HP inicial del juego.
    public Integer getInitialHp() {
        return initialHp;
    }

    // Setter de HP inicial del juego.
    public void setInitialHp(Integer initialHp) {
        this.initialHp = initialHp;
    }

    // Getter de tasa base de spawn.
    public Double getBaseSpawnRate() {
        return baseSpawnRate;
    }

    // Setter de tasa base de spawn.
    public void setBaseSpawnRate(Double baseSpawnRate) {
        this.baseSpawnRate = baseSpawnRate;
    }

    // Getter de velocidad base de ataque.
    public Double getBaseAttackSpeed() {
        return baseAttackSpeed;
    }

    // Setter de velocidad base de ataque.
    public void setBaseAttackSpeed(Double baseAttackSpeed) {
        this.baseAttackSpeed = baseAttackSpeed;
    }

    // Getter de score por kill.
    public Integer getScorePerKill() {
        return scorePerKill;
    }

    // Setter de score por kill.
    public void setScorePerKill(Integer scorePerKill) {
        this.scorePerKill = scorePerKill;
    }

    // Getter del puntaje para subir dificultad.
    public Integer getDifficultyStepScore() {
        return difficultyStepScore;
    }

    // Setter del puntaje para subir dificultad.
    public void setDifficultyStepScore(Integer difficultyStepScore) {
        this.difficultyStepScore = difficultyStepScore;
    }

    // Getter del multiplicador de spawn por nivel.
    public Double getSpawnMultiplierPerLevel() {
        return spawnMultiplierPerLevel;
    }

    // Setter del multiplicador de spawn por nivel.
    public void setSpawnMultiplierPerLevel(Double spawnMultiplierPerLevel) {
        this.spawnMultiplierPerLevel = spawnMultiplierPerLevel;
    }

    // Getter del incremento de velocidad por nivel.
    public Double getSpeedAddPerLevel() {
        return speedAddPerLevel;
    }

    // Setter del incremento de velocidad por nivel.
    public void setSpeedAddPerLevel(Double speedAddPerLevel) {
        this.speedAddPerLevel = speedAddPerLevel;
    }

    // Getter del daño tipo DDOS.
    public Integer getDamageDDOS() {
        return damageDDOS;
    }

    // Setter del daño tipo DDOS.
    public void setDamageDDOS(Integer damageDDOS) {
        this.damageDDOS = damageDDOS;
    }

    // Getter del daño tipo MALWARE.
    public Integer getDamageMALWARE() {
        return damageMALWARE;
    }

    // Setter del daño tipo MALWARE.
    public void setDamageMALWARE(Integer damageMALWARE) {
        this.damageMALWARE = damageMALWARE;
    }

    // Getter del daño tipo CRED.
    public Integer getDamageCRED() {
        return damageCRED;
    }

    // Setter del daño tipo CRED.
    public void setDamageCRED(Integer damageCRED) {
        this.damageCRED = damageCRED;
    }
}