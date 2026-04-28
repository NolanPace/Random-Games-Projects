import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;

public class TowerDefense extends JFrame {
    public TowerDefense() {
        add(new TDPanel());
        setTitle("Tower Defense");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        new TowerDefense();
    }
}

class TDPanel extends JPanel implements ActionListener, MouseListener {
    private final int WIDTH = 850;
    private final int HEIGHT = 620;
    private Timer timer = new Timer(16, this);

    private ArrayList<TDEnemy> enemies = new ArrayList<>();
    private ArrayList<TDTower> towers = new ArrayList<>();
    private ArrayList<TDShot> shots = new ArrayList<>();
    private Point[] path = {
        new Point(0, 310), new Point(220, 310), new Point(220, 130),
        new Point(540, 130), new Point(540, 440), new Point(850, 440)
    };

    private int money = 125, lives = 12, score = 0, wave = 1, spawnTimer = 0, anim = 0;
    private boolean gameOver = false;

    public TDPanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        addMouseListener(this);
        timer.start();
    }

    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setPaint(new GradientPaint(0, 0, new Color(25, 80, 45), 0, HEIGHT, new Color(12, 45, 35)));
        g2.fillRect(0, 0, WIDTH, HEIGHT);

        drawTerrain(g2);
        drawPath(g2);

        for (TDTower t : towers) t.draw(g2);
        for (TDEnemy e : enemies) e.draw(g2);
        for (TDShot s : shots) s.draw(g2);

        drawHUD(g2);

        if (gameOver) {
            g2.setColor(new Color(0,0,0,180));
            g2.fillRect(0, 250, WIDTH, 100);
            g2.setColor(Color.RED);
            g2.setFont(new Font("Arial", Font.BOLD, 44));
            g2.drawString("GAME OVER", 290, 315);
        }
    }

    private void drawTerrain(Graphics2D g2) {
        g2.setColor(new Color(20, 65, 35));
        for (int i = 0; i < 25; i++) {
            int x = (i * 83) % WIDTH;
            int y = 65 + (i * 47) % 500;
            g2.fillOval(x, y, 20, 12);
        }
    }

    private void drawPath(Graphics2D g2) {
        g2.setStroke(new BasicStroke(58, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(95, 70, 45));
        for (int i = 0; i < path.length - 1; i++) g2.drawLine(path[i].x, path[i].y, path[i+1].x, path[i+1].y);

        g2.setStroke(new BasicStroke(40, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(145, 110, 70));
        for (int i = 0; i < path.length - 1; i++) g2.drawLine(path[i].x, path[i].y, path[i+1].x, path[i+1].y);

        g2.setStroke(new BasicStroke(1));
    }

    private void drawHUD(Graphics2D g2) {
        g2.setColor(new Color(0,0,0,170));
        g2.fillRoundRect(10, 10, 820, 42, 15, 15);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 18));
        g2.drawString("Money: $" + money, 25, 37);
        g2.drawString("Lives: " + lives, 165, 37);
        g2.drawString("Score: " + score, 275, 37);
        g2.drawString("Wave: " + wave, 390, 37);
        g2.drawString("Click to place cannon tower ($25)", 510, 37);
    }

    public void actionPerformed(ActionEvent e) {
        anim++;
        if (!gameOver) {
            spawn();
            updateEnemies();
            updateTowers();
            updateShots();
            if (lives <= 0) gameOver = true;
        }
        repaint();
    }

    private void spawn() {
        spawnTimer++;
        if (spawnTimer > Math.max(22, 82 - wave * 5)) {
            enemies.add(new TDEnemy(path, wave));
            spawnTimer = 0;
        }
    }

    private void updateEnemies() {
        for (int i = enemies.size()-1; i >= 0; i--) {
            TDEnemy enemy = enemies.get(i);
            enemy.move();
            if (enemy.finished) {
                lives--;
                enemies.remove(i);
            } else if (enemy.health <= 0) {
                money += 10;
                score += 10;
                if (score % 120 == 0) wave++;
                enemies.remove(i);
            }
        }
    }

    private void updateTowers() {
        for (TDTower t : towers) {
            t.cooldown--;
            if (t.cooldown <= 0) {
                TDEnemy target = null;
                for (TDEnemy enemy : enemies) {
                    if (Math.hypot(t.x - enemy.x, t.y - enemy.y) <= t.range) {
                        target = enemy;
                        break;
                    }
                }
                if (target != null) {
                    shots.add(new TDShot(t.x, t.y, target));
                    t.cooldown = 30;
                }
            }
        }
    }

    private void updateShots() {
        for (int i = shots.size()-1; i >= 0; i--) {
            TDShot s = shots.get(i);
            s.move();
            if (s.hit) {
                s.target.health--;
                shots.remove(i);
            }
        }
    }

    public void mousePressed(MouseEvent e) {
        if (!gameOver && money >= 25) {
            towers.add(new TDTower(e.getX(), e.getY()));
            money -= 25;
        }
    }

    public void mouseReleased(MouseEvent e) {}
    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
}

class TDEnemy {
    double x, y, speed;
    int health, maxHealth;
    int targetIndex = 1;
    boolean finished = false;
    Point[] path;

    TDEnemy(Point[] path, int wave) {
        this.path = path;
        this.x = path[0].x;
        this.y = path[0].y;
        this.health = 3 + wave;
        this.maxHealth = health;
        this.speed = 1.25 + wave * 0.09;
    }

    void move() {
        if (targetIndex >= path.length) {
            finished = true;
            return;
        }
        Point target = path[targetIndex];
        double dx = target.x - x, dy = target.y - y;
        double dist = Math.hypot(dx, dy);
        if (dist < speed) {
            x = target.x; y = target.y; targetIndex++;
        } else {
            x += speed * dx / dist;
            y += speed * dy / dist;
        }
    }

    void draw(Graphics2D g2) {
        g2.setColor(new Color(0,0,0,90));
        g2.fillOval((int)x-15, (int)y+9, 30, 10);
        g2.setColor(new Color(165, 45, 45));
        g2.fillOval((int)x-14, (int)y-14, 28, 28);
        g2.setColor(new Color(255, 160, 160));
        g2.fillOval((int)x-7, (int)y-8, 5, 5);
        g2.fillOval((int)x+3, (int)y-8, 5, 5);

        g2.setColor(Color.BLACK);
        g2.fillRect((int)x-18, (int)y-25, 36, 5);
        g2.setColor(Color.GREEN);
        g2.fillRect((int)x-18, (int)y-25, Math.max(0, 36 * health / maxHealth), 5);
    }
}

class TDTower {
    int x, y, range = 125, cooldown = 0;
    TDTower(int x, int y) { this.x = x; this.y = y; }

    void draw(Graphics2D g2) {
        g2.setColor(new Color(255,255,255,28));
        g2.fillOval(x-range, y-range, range*2, range*2);
        g2.setColor(new Color(30, 40, 85));
        g2.fillOval(x-22, y-22, 44, 44);
        g2.setColor(new Color(75, 110, 210));
        g2.fillOval(x-16, y-16, 32, 32);
        g2.setColor(Color.DARK_GRAY);
        g2.fillRect(x-5, y-34, 10, 32);
        g2.setColor(Color.WHITE);
        g2.drawOval(x-22, y-22, 44, 44);
    }
}

class TDShot {
    double x, y, speed = 9;
    TDEnemy target;
    boolean hit = false;
    TDShot(double x, double y, TDEnemy target) { this.x = x; this.y = y; this.target = target; }

    void move() {
        double dx = target.x - x, dy = target.y - y;
        double dist = Math.hypot(dx, dy);
        if (dist < speed) hit = true;
        else { x += speed * dx / dist; y += speed * dy / dist; }
    }

    void draw(Graphics2D g2) {
        g2.setColor(Color.YELLOW);
        g2.fillOval((int)x-5, (int)y-5, 10, 10);
        g2.setColor(Color.WHITE);
        g2.fillOval((int)x-2, (int)y-2, 4, 4);
    }
}
