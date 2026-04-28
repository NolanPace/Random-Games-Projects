import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;

public class TowerDefense extends JFrame {
    public TowerDefense() {
        add(new TDPanel());
        setTitle("Java Tower Defense");
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
    private final int WIDTH = 800;
    private final int HEIGHT = 600;
    private Timer timer = new Timer(16, this);

    private ArrayList<Enemy> enemies = new ArrayList<>();
    private ArrayList<Tower> towers = new ArrayList<>();
    private ArrayList<Shot> shots = new ArrayList<>();

    private int money = 100;
    private int lives = 10;
    private int score = 0;
    private int spawnTimer = 0;
    private int wave = 1;
    private boolean gameOver = false;

    private Point[] path = {
            new Point(0, 300), new Point(200, 300), new Point(200, 120),
            new Point(500, 120), new Point(500, 420), new Point(800, 420)
    };

    public TDPanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        addMouseListener(this);
        timer.start();
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        drawPath(g);

        for (Tower t : towers) t.draw(g);
        for (Enemy e : enemies) e.draw(g);
        for (Shot s : shots) s.draw(g);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Money: $" + money, 20, 25);
        g.drawString("Lives: " + lives, 160, 25);
        g.drawString("Score: " + score, 270, 25);
        g.drawString("Click to place tower ($25)", 450, 25);

        if (gameOver) {
            g.setFont(new Font("Arial", Font.BOLD, 45));
            g.drawString("GAME OVER", 270, 300);
        }
    }

    private void drawPath(Graphics g) {
        g.setColor(Color.DARK_GRAY);
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(45));

        for (int i = 0; i < path.length - 1; i++) {
            g2.drawLine(path[i].x, path[i].y, path[i + 1].x, path[i + 1].y);
        }

        g2.setStroke(new BasicStroke(1));
    }

    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            spawnEnemies();
            updateEnemies();
            updateTowers();
            updateShots();
            checkGameOver();
        }

        repaint();
    }

    private void spawnEnemies() {
        spawnTimer++;
        if (spawnTimer > Math.max(25, 90 - wave * 5)) {
            enemies.add(new Enemy(path, wave));
            spawnTimer = 0;
            if (score > 0 && score % 100 == 0) wave++;
        }
    }

    private void updateEnemies() {
        for (int i = enemies.size() - 1; i >= 0; i--) {
            Enemy enemy = enemies.get(i);
            enemy.move();

            if (enemy.finished) {
                lives--;
                enemies.remove(i);
            } else if (enemy.health <= 0) {
                money += 10;
                score += 10;
                enemies.remove(i);
            }
        }
    }

    private void updateTowers() {
        for (Tower t : towers) {
            t.cooldown--;

            if (t.cooldown <= 0) {
                Enemy target = null;

                for (Enemy e : enemies) {
                    double d = Math.hypot(t.x - e.x, t.y - e.y);
                    if (d <= t.range) {
                        target = e;
                        break;
                    }
                }

                if (target != null) {
                    shots.add(new Shot(t.x, t.y, target));
                    t.cooldown = 35;
                }
            }
        }
    }

    private void updateShots() {
        for (int i = shots.size() - 1; i >= 0; i--) {
            Shot s = shots.get(i);
            s.move();

            if (s.hit) {
                s.target.health -= 1;
                shots.remove(i);
            }
        }
    }

    private void checkGameOver() {
        if (lives <= 0) {
            gameOver = true;
        }
    }

    public void mousePressed(MouseEvent e) {
        if (money >= 25 && !gameOver) {
            towers.add(new Tower(e.getX(), e.getY()));
            money -= 25;
        }
    }

    public void mouseReleased(MouseEvent e) {}
    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
}

class Enemy {
    double x, y;
    int health;
    int targetIndex = 1;
    boolean finished = false;
    Point[] path;
    double speed;

    Enemy(Point[] path, int wave) {
        this.path = path;
        this.x = path[0].x;
        this.y = path[0].y;
        this.health = 3 + wave;
        this.speed = 1.2 + wave * 0.08;
    }

    void move() {
        if (targetIndex >= path.length) {
            finished = true;
            return;
        }

        Point target = path[targetIndex];
        double dx = target.x - x;
        double dy = target.y - y;
        double dist = Math.hypot(dx, dy);

        if (dist < speed) {
            x = target.x;
            y = target.y;
            targetIndex++;
        } else {
            x += speed * dx / dist;
            y += speed * dy / dist;
        }
    }

    void draw(Graphics g) {
        g.setColor(Color.RED);
        g.fillOval((int)x - 12, (int)y - 12, 24, 24);
        g.setColor(Color.WHITE);
        g.drawString("" + health, (int)x - 5, (int)y + 5);
    }
}

class Tower {
    int x, y;
    int range = 120;
    int cooldown = 0;

    Tower(int x, int y) {
        this.x = x;
        this.y = y;
    }

    void draw(Graphics g) {
        g.setColor(Color.BLUE);
        g.fillRect(x - 15, y - 15, 30, 30);
        g.setColor(new Color(255, 255, 255, 40));
        g.drawOval(x - range, y - range, range * 2, range * 2);
    }
}

class Shot {
    double x, y;
    Enemy target;
    boolean hit = false;
    double speed = 8;

    Shot(double x, double y, Enemy target) {
        this.x = x;
        this.y = y;
        this.target = target;
    }

    void move() {
        double dx = target.x - x;
        double dy = target.y - y;
        double dist = Math.hypot(dx, dy);

        if (dist < speed) {
            hit = true;
        } else {
            x += speed * dx / dist;
            y += speed * dy / dist;
        }
    }

    void draw(Graphics g) {
        g.setColor(Color.YELLOW);
        g.fillOval((int)x - 4, (int)y - 4, 8, 8);
    }
}
