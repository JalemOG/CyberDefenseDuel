package client;

import common.Modelos.Message;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GameController {

    @FXML private AnchorPane root;
    @FXML private Circle myCircle;
    
    // Tus stats
    @FXML private Label playerHp;
    @FXML private Label playerScore;
    @FXML private Label levelLabel;
    
    // Stats del oponente (Recibidas por red)
    @FXML private Label enemyHp;
    @FXML private Label enemyScore;

    // --- Parámetros de Juego Originales ---
    private static final int initialHp = 100;
    private static final double baseSpawnRate = 1.0;
    private static final double baseAttackSpeed = 10.0;
    private static final int scorePerKill = 10;
    private static final int difficultyStepScore = 100;
    private static final double spawnMultiplierPerLevel = 1.15;
    private static final double speedAddPerLevel = 0.5;
    private static final Map<String, Integer> damageByType = Map.of(
            "DDOS", 5, "MALWARE", 8, "CRED", 10
    );
    private static final double MIN_X = 81;
    private static final double MAX_X = 231;

    private final List<Enemy> enemies = new ArrayList<>();
    private final Random random = new Random();
    private AnimationTimer gameLoop;
    private long lastUpdate = 0;
    private double spawnAccumulator = 0.0;

    private int hp = initialHp;
    private int score = 0;
    private int level = 1;

    private double currentSpawnRate = baseSpawnRate;
    private double currentSpeed = baseAttackSpeed;
    private boolean gameRunning = true;

    @FXML
    public void initialize() {
        // 1. Conectar con el servidor
        NetworkManager.getInstance().setMessageHandler(this::handleServerMessage);
        
        // 2. Preparar UI
        updateUi();
        enemyHp.setText("HP:\n100");
        enemyScore.setText("SCORE:\n0");

       
        // Le decimos a toda la ventana que escuche el teclado sin importar dónde esté el clic
        Platform.runLater(() -> {
            root.getScene().setOnKeyPressed(this::handleKeyPress);
        });

        // 3. Iniciar el Motor
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!gameRunning) return;
                if (lastUpdate == 0) { lastUpdate = now; return; }
                double deltaSeconds = (now - lastUpdate) / 1_000_000_000.0;
                lastUpdate = now;
                updateGame(deltaSeconds);
            }
        };
        gameLoop.start();
    }

    private void handleServerMessage(Message msg) {
        if (msg.getType() == null) return;

        Platform.runLater(() -> {
            switch (msg.getType()) {
                case "OPPONENT_STATS":
                    enemyHp.setText("HP:\n" + msg.getHp());
                    enemyScore.setText("SCORE:\n" + msg.getScore());
                    break;
                case "OPPONENT_DIED":
                    victory();
                    break;
            }
        });
    }

    private void broadcastStats() {
        Message msg = new Message("SYNC_STATS");
        msg.setHp(this.hp);
        msg.setScore(this.score);
        NetworkManager.getInstance().sendMessage(msg);
    }

    // ========================== LÓGICA DEL JUEGO ==========================
    private void updateGame(double deltaSeconds) {
        spawnAccumulator += deltaSeconds;
        double spawnInterval = 1.0 / currentSpawnRate;
        while (spawnAccumulator >= spawnInterval) {
            spawnEnemy();
            spawnAccumulator -= spawnInterval;
        }

        Iterator<Enemy> iterator = enemies.iterator();
        while (iterator.hasNext()) {
            Enemy enemy = iterator.next();
            enemy.y -= currentSpeed * deltaSeconds;

            // Enemigo impacta tu base
            if (enemy.y <= 0) {
                hp -= damageByType.get(enemy.type);
                if (hp < 0) hp = 0;
                
                updateUi();
                broadcastStats(); // <-- AVISAMOS AL SERVIDOR QUE NOS HIRIERON

                root.getChildren().remove(enemy.node);
                iterator.remove();

                if (hp <= 0) {
                    gameOver();
                    return;
                }
                continue;
            }
            enemy.node.setLayoutY(enemy.y);
        }

        // Subir niveles
        int newLevel = (score / difficultyStepScore) + 1;
        if (newLevel > level) {
            level = newLevel;
            currentSpawnRate = baseSpawnRate * Math.pow(spawnMultiplierPerLevel, level - 1);
            currentSpeed = baseAttackSpeed + (level - 1) * speedAddPerLevel;
            updateUi();
        }
    }

    private void spawnEnemy() {
        String[] types = {"DDOS", "MALWARE", "CRED"};
        String type = types[random.nextInt(types.length)];
        Integer[] spawns = {61, 111, 161, 211};
        Integer spawn = spawns[random.nextInt(spawns.length)];

        Label label = new Label(type.substring(0, 4));
        label.setTextFill(Color.WHITE);
        label.setStyle("-fx-font-weight: bold; -fx-background-color: " +
                (type.equals("DDOS") ? "pink" : type.equals("MALWARE") ? "purple" : "black") +
                "; -fx-padding: 4; -fx-background-radius: 5;");
        label.setLayoutX(spawn);
        label.setLayoutY(root.getPrefHeight());

        root.getChildren().add(label);
        enemies.add(new Enemy(type, spawn, root.getPrefHeight(), spawn, label));
    }

    @FXML
    public void handleKeyPress(KeyEvent event) {
        if (!gameRunning) return;
        KeyCode code = event.getCode();

        if (code == KeyCode.LEFT) {
            double newX = myCircle.getLayoutX() - 50;
            if (newX >= MIN_X) myCircle.setLayoutX(newX);
        } else if (code == KeyCode.RIGHT) {
            double newX = myCircle.getLayoutX() + 50;
            if (newX <= MAX_X) myCircle.setLayoutX(newX);
        } 
        else if (code == KeyCode.Q || code == KeyCode.W || code == KeyCode.E) {
            String targetType = switch (code) {
                case Q -> "DDOS";
                case W -> "MALWARE";
                case E -> "CRED";
                default -> null;
            };

            for (Enemy enemy : enemies) {
                if (enemy.type.equals(targetType) && enemy.spawn == myCircle.getLayoutX() - 20) {
                    root.getChildren().remove(enemy.node);
                    enemies.remove(enemy);
                    
                    score += scorePerKill;
                    updateUi();
                    broadcastStats(); // <-- AVISAMOS AL SERVIDOR QUE SUBIMOS DE PUNTOS
                    break;
                }
            }
        }
        event.consume();
    }

    private void updateUi() {
        playerHp.setText("HP:\n" + hp);
        playerScore.setText("SCORE:\n" + score);
        levelLabel.setText("lvl. " + level);
    }

    // ========================== CLASE INTERNA ==========================
    private static class Enemy {
        Integer spawn;
        String type;
        double x, y;
        Label node;

        Enemy(String type, double x, double y, Integer spawn, Label node) {
            this.type = type; this.spawn = spawn; this.x = x; this.y = y; this.node = node;
        }
    }

    // ========================== PANTALLAS FINALES ==========================
    private void gameOver() {
        gameRunning = false;
        gameLoop.stop();
        NetworkManager.getInstance().sendMessage(new Message("I_DIED")); // Avisar derrota
        showEndScreen("GAME OVER", Color.RED);
    }

    private void victory() {
        gameRunning = false;
        gameLoop.stop();
        showEndScreen("VICTORY!", Color.GREEN);
    }

    private void showEndScreen(String title, Color color) {
        Rectangle bg = new Rectangle(800, 300, Color.BLACK);
        bg.setOpacity(0.8);

        Label lblTitle = new Label(title);
        lblTitle.setTextFill(color);
        lblTitle.setStyle("-fx-font-size: 50; -fx-font-weight: bold;");
        lblTitle.setLayoutX(250); lblTitle.setLayoutY(50);

        Label stats = new Label("Score: " + score + "\nLevel: " + level);
        stats.setTextFill(Color.WHITE);
        stats.setStyle("-fx-font-size: 30;");
        stats.setLayoutX(330); stats.setLayoutY(130);

        root.getChildren().addAll(bg, lblTitle, stats);
    }
}