import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class RPG extends JFrame {
    public RPG() {
        add(new RPGPanel());
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

class RPGPanel extends JPanel implements ActionListener, KeyListener {
    private final int TILE = 40;
    private final int WIDTH = 800;
    private final int HEIGHT = 600;

    private final javax.swing.Timer timer = new javax.swing.Timer(16, this);
    private boolean up, down, left, right;
    private int playerX = 400, playerY = 300;
    private int playerSize = 30;
    private int health = 5;
    private int coins = 0;
    private int enemyX = 210, enemyY = 210;
    private int enemyHealth = 4;
    private int anim = 0;
    private boolean gameOver = false, win = false;

    private int[][] map = {
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
        {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,1},
        {1,0,4,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,1},
        {1,0,0,1,1,1,0,0,0,0,0,0,1,1,1,0,0,4,0,1},
        {1,0,0,1,0,0,0,4,0,3,0,0,0,0,1,0,0,0,0,1},
        {1,0,0,1,0,0,0,0,0,0,0,0,0,0,1,0,4,0,0,1},
        {1,0,0,1,0,4,0,0,0,0,0,0,4,0,1,0,0,0,0,1},
        {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
        {1,0,4,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,1},
        {1,0,0,0,0,0,1,1,1,1,1,1,1,0,0,0,0,4,0,1},
        {1,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,1},
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}
    };

    public RPGPanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        addKeyListener(this);
        timer.start();
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawWorld(g2);
        drawEnemy(g2);
        drawPlayer(g2);
        drawHUD(g2);

        if (gameOver) drawCenter(g2, "GAME OVER", Color.RED);
        if (win) drawCenter(g2, "YOU FOUND THE PORTAL!", Color.CYAN);
    }

    private void drawWorld(Graphics2D g2) {
        GradientPaint bg = new GradientPaint(0, 0, new Color(30, 95, 40), 0, HEIGHT, new Color(12, 55, 30));
        g2.setPaint(bg);
        g2.fillRect(0, 0, WIDTH, HEIGHT);

        for (int r = 0; r < map.length; r++) {
            for (int c = 0; c < map[0].length; c++) {
                int x = c * TILE, y = r * TILE;

                if (map[r][c] == 1) {
                    g2.setColor(new Color(80, 50, 25));
                    g2.fillRoundRect(x, y, TILE, TILE, 10, 10);
                    g2.setColor(new Color(120, 80, 40));
                    g2.fillRoundRect(x + 5, y + 5, TILE - 10, TILE - 10, 8, 8);
                } else {
                    g2.setColor(new Color(55, 135, 55));
                    g2.fillRect(x, y, TILE, TILE);
                    g2.setColor(new Color(70, 160, 70, 120));
                    g2.fillOval(x + 6, y + 8, 6, 6);
                    g2.fillOval(x + 24, y + 25, 5, 5);
                }

                if (map[r][c] == 2) drawCoin(g2, x + 20, y + 20);
                if (map[r][c] == 3) drawPortal(g2, x + 20, y + 20);
                if (map[r][c] == 4) drawTree(g2, x, y);
            }
        }
    }

    private void drawCoin(Graphics2D g2, int cx, int cy) {
        g2.setColor(new Color(255, 210, 0));
        g2.fillOval(cx - 10, cy - 10, 20, 20);
        g2.setColor(new Color(255, 245, 140));
        g2.fillOval(cx - 5, cy - 6, 7, 7);
        g2.setColor(new Color(120, 80, 0));
        g2.drawOval(cx - 10, cy - 10, 20, 20);
    }

    private void drawPortal(Graphics2D g2, int cx, int cy) {
        int pulse = (int)(Math.sin(anim * 0.1) * 5);
        g2.setColor(new Color(0, 255, 255, 120));
        g2.fillOval(cx - 17 - pulse, cy - 17 - pulse, 34 + pulse * 2, 34 + pulse * 2);
        g2.setColor(new Color(0, 80, 180));
        g2.fillOval(cx - 13, cy - 13, 26, 26);
        g2.setColor(Color.WHITE);
        g2.drawArc(cx - 10, cy - 10, 20, 20, anim * 8, 240);
    }

    private void drawTree(Graphics2D g2, int x, int y) {
        g2.setColor(new Color(80, 45, 20));
        g2.fillRect(x + 17, y + 20, 7, 18);
        g2.setColor(new Color(25, 100, 35));
        g2.fillOval(x + 7, y + 5, 26, 26);
        g2.setColor(new Color(40, 135, 45));
        g2.fillOval(x + 13, y, 20, 20);
    }

    private void drawPlayer(Graphics2D g2) {
        g2.setColor(new Color(20, 20, 20, 90));
        g2.fillOval(playerX - 3, playerY + 24, 36, 12);

        g2.setColor(new Color(40, 90, 230));
        g2.fillRoundRect(playerX, playerY + 8, playerSize, playerSize - 4, 12, 12);

        g2.setColor(new Color(245, 205, 160));
        g2.fillOval(playerX + 5, playerY, 20, 20);

        g2.setColor(new Color(20, 60, 160));
        g2.fillPolygon(new int[]{playerX + 6, playerX + 15, playerX + 24}, new int[]{playerY + 4, playerY - 13, playerY + 4}, 3);

        g2.setColor(Color.WHITE);
        g2.fillOval(playerX + 9, playerY + 8, 4, 4);
        g2.fillOval(playerX + 18, playerY + 8, 4, 4);

        g2.setColor(Color.LIGHT_GRAY);
        g2.fillRect(playerX + 28, playerY + 14, 18, 5);
        g2.setColor(Color.WHITE);
        g2.drawRect(playerX + 28, playerY + 14, 18, 5);
    }

    private void drawEnemy(Graphics2D g2) {
        if (enemyHealth <= 0) return;

        g2.setColor(new Color(20, 20, 20, 90));
        g2.fillOval(enemyX - 3, enemyY + 24, 36, 12);
        g2.setColor(new Color(170, 20, 30));
        g2.fillOval(enemyX, enemyY, 32, 32);
        g2.setColor(new Color(80, 0, 0));
        g2.fillOval(enemyX + 6, enemyY + 9, 6, 6);
        g2.fillOval(enemyX + 20, enemyY + 9, 6, 6);
        g2.setColor(Color.WHITE);
        g2.drawString("HP " + enemyHealth, enemyX - 3, enemyY - 5);
    }

    private void drawHUD(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRoundRect(10, 10, 455, 38, 15, 15);

        for (int i = 0; i < 5; i++) {
            g2.setColor(i < health ? Color.RED : Color.DARK_GRAY);
            g2.fillOval(25 + i * 28, 18, 20, 20);
        }

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 17));
        g2.drawString("Coins: " + coins, 180, 35);
        g2.drawString("Move: WASD/Arrows   Attack: SPACE", 285, 35);
    }

    private void drawCenter(Graphics2D g2, String text, Color color) {
        g2.setColor(new Color(0, 0, 0, 170));
        g2.fillRect(0, 250, WIDTH, 100);
        g2.setColor(color);
        g2.setFont(new Font("Arial", Font.BOLD, 42));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(text, (WIDTH - fm.stringWidth(text)) / 2, 315);
    }

    public void actionPerformed(ActionEvent e) {
        anim++;
        if (!gameOver && !win) {
            move();
            checkTiles();
            checkEnemy();
        }
        repaint();
    }

    private void move() {
        int nx = playerX, ny = playerY;
        if (up) ny -= 5;
        if (down) ny += 5;
        if (left) nx -= 5;
        if (right) nx += 5;

        if (!hitsWall(nx, ny)) {
            playerX = nx;
            playerY = ny;
        }
    }

    private boolean hitsWall(int x, int y) {
        Rectangle player = new Rectangle(x, y, playerSize, playerSize);
        for (int r = 0; r < map.length; r++) {
            for (int c = 0; c < map[0].length; c++) {
                if (map[r][c] == 1) {
                    Rectangle wall = new Rectangle(c * TILE, r * TILE, TILE, TILE);
                    if (player.intersects(wall)) return true;
                }
            }
        }
        return false;
    }

    private void checkTiles() {
        int row = (playerY + playerSize / 2) / TILE;
        int col = (playerX + playerSize / 2) / TILE;

        if (map[row][col] == 2) {
            coins++;
            map[row][col] = 0;
        }
        if (map[row][col] == 3) win = true;
    }

    private void checkEnemy() {
        if (enemyHealth <= 0) return;
        Rectangle player = new Rectangle(playerX, playerY, playerSize, playerSize);
        Rectangle enemy = new Rectangle(enemyX, enemyY, 32, 32);
        if (player.intersects(enemy)) {
            health--;
            playerX = 400;
            playerY = 300;
            if (health <= 0) gameOver = true;
        }
    }

    private void attack() {
        if (enemyHealth <= 0) return;
        Rectangle attack = new Rectangle(playerX - 25, playerY - 25, playerSize + 50, playerSize + 50);
        Rectangle enemy = new Rectangle(enemyX, enemyY, 32, 32);
        if (attack.intersects(enemy)) enemyHealth--;
    }

    public void keyPressed(KeyEvent e) {
        int k = e.getKeyCode();
        if (k == KeyEvent.VK_W || k == KeyEvent.VK_UP) up = true;
        if (k == KeyEvent.VK_S || k == KeyEvent.VK_DOWN) down = true;
        if (k == KeyEvent.VK_A || k == KeyEvent.VK_LEFT) left = true;
        if (k == KeyEvent.VK_D || k == KeyEvent.VK_RIGHT) right = true;
        if (k == KeyEvent.VK_SPACE) attack();
    }

    public void keyReleased(KeyEvent e) {
        int k = e.getKeyCode();
        if (k == KeyEvent.VK_W || k == KeyEvent.VK_UP) up = false;
        if (k == KeyEvent.VK_S || k == KeyEvent.VK_DOWN) down = false;
        if (k == KeyEvent.VK_A || k == KeyEvent.VK_LEFT) left = false;
        if (k == KeyEvent.VK_D || k == KeyEvent.VK_RIGHT) right = false;
    }

    public void keyTyped(KeyEvent e) {}
}
