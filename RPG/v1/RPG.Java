import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class RPG extends JFrame {

    public RPG() {
        add(new GamePanel());
        setTitle("Mini RPG");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        new RPG();
    }
}

class GamePanel extends JPanel implements ActionListener, KeyListener {

    private final int WIDTH = 800;
    private final int HEIGHT = 600;
    private final int TILE = 40;

    private Timer timer;

    private int playerX = 400;
    private int playerY = 300;
    private int playerSize = 30;
    private int speed = 5;

    private int enemyX = 200;
    private int enemyY = 200;
    private int enemyHealth = 3;

    private int playerHealth = 5;
    private int coins = 0;

    private boolean up, down, left, right;
    private boolean gameOver = false;
    private boolean win = false;

    private int[][] map = {
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,1,1,1,0,0,0,0,0,0,1,1,1,0,0,0,0,1},
            {1,0,0,1,0,0,0,0,0,3,0,0,0,0,1,0,0,0,0,1},
            {1,0,0,1,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,1},
            {1,0,0,1,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,1,1,1,1,1,1,1,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}
    };

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        timer = new Timer(16, this);
        timer.start();
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        drawMap(g);
        drawPlayer(g);
        drawEnemy(g);
        drawHUD(g);

        if (gameOver) {
            drawCenteredText(g, "GAME OVER", 300);
        }

        if (win) {
            drawCenteredText(g, "YOU WIN!", 300);
        }
    }

    private void drawMap(Graphics g) {
        for (int row = 0; row < map.length; row++) {
            for (int col = 0; col < map[0].length; col++) {
                int x = col * TILE;
                int y = row * TILE;

                if (map[row][col] == 0) {
                    g.setColor(new Color(60, 150, 60));
                } else if (map[row][col] == 1) {
                    g.setColor(new Color(90, 60, 30));
                } else if (map[row][col] == 2) {
                    g.setColor(Color.YELLOW);
                } else if (map[row][col] == 3) {
                    g.setColor(Color.CYAN);
                }

                g.fillRect(x, y, TILE, TILE);
                g.setColor(Color.BLACK);
                g.drawRect(x, y, TILE, TILE);
            }
        }
    }

    private void drawPlayer(Graphics g) {
        g.setColor(Color.BLUE);
        g.fillRect(playerX, playerY, playerSize, playerSize);
    }

    private void drawEnemy(Graphics g) {
        if (enemyHealth > 0) {
            g.setColor(Color.RED);
            g.fillRect(enemyX, enemyY, 30, 30);
        }
    }

    private void drawHUD(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Health: " + playerHealth, 20, 25);
        g.drawString("Coins: " + coins, 150, 25);
        g.drawString("WASD/Arrows = Move | SPACE = Attack", 300, 25);
    }

    private void drawCenteredText(Graphics g, String text, int y) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 50));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, (WIDTH - fm.stringWidth(text)) / 2, y);
    }

    public void actionPerformed(ActionEvent e) {
        if (!gameOver && !win) {
            movePlayer();
            checkSpecialTiles();
            checkEnemyCollision();
        }

        repaint();
    }

    private void movePlayer() {
        int newX = playerX;
        int newY = playerY;

        if (up) newY -= speed;
        if (down) newY += speed;
        if (left) newX -= speed;
        if (right) newX += speed;

        if (!hitsWall(newX, newY)) {
            playerX = newX;
            playerY = newY;
        }
    }

    private boolean hitsWall(int x, int y) {
        Rectangle playerRect = new Rectangle(x, y, playerSize, playerSize);

        for (int row = 0; row < map.length; row++) {
            for (int col = 0; col < map[0].length; col++) {
                if (map[row][col] == 1) {
                    Rectangle wall = new Rectangle(col * TILE, row * TILE, TILE, TILE);

                    if (playerRect.intersects(wall)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private void checkSpecialTiles() {
        int centerX = playerX + playerSize / 2;
        int centerY = playerY + playerSize / 2;

        int col = centerX / TILE;
        int row = centerY / TILE;

        if (map[row][col] == 2) {
            coins++;
            map[row][col] = 0;
        }

        if (map[row][col] == 3) {
            win = true;
        }
    }

    private void checkEnemyCollision() {
        if (enemyHealth <= 0) return;

        Rectangle player = new Rectangle(playerX, playerY, playerSize, playerSize);
        Rectangle enemy = new Rectangle(enemyX, enemyY, 30, 30);

        if (player.intersects(enemy)) {
            playerHealth--;

            playerX = 400;
            playerY = 300;

            if (playerHealth <= 0) {
                gameOver = true;
            }
        }
    }

    private void attack() {
        if (enemyHealth <= 0) return;

        Rectangle attackBox = new Rectangle(playerX - 20, playerY - 20, playerSize + 40, playerSize + 40);
        Rectangle enemy = new Rectangle(enemyX, enemyY, 30, 30);

        if (attackBox.intersects(enemy)) {
            enemyHealth--;
        }
    }

    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_W || key == KeyEvent.VK_UP) up = true;
        if (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN) down = true;
        if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT) left = true;
        if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT) right = true;

        if (key == KeyEvent.VK_SPACE) {
            attack();
        }
    }

    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_W || key == KeyEvent.VK_UP) up = false;
        if (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN) down = false;
        if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT) left = false;
        if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT) right = false;
    }

    public void keyTyped(KeyEvent e) {
    }
}