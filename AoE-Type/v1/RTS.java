import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class RTS extends JFrame {
    public RTS() {
        add(new RTSPanel());
        setTitle("Mini Age of Empires Style RTS");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        new RTS();
    }
}

class RTSPanel extends JPanel implements ActionListener, MouseListener, KeyListener {
    private final int WIDTH = 900;
    private final int HEIGHT = 650;

    private Timer timer = new Timer(16, this);
    private ArrayList<Unit> units = new ArrayList<>();
    private ArrayList<Unit> enemies = new ArrayList<>();
    private ArrayList<Building> buildings = new ArrayList<>();

    private Unit selected = null;
    private int wood = 200;
    private int food = 100;
    private int spawnTimer = 0;
    private boolean gameOver = false;
    private String mode = "select";

    public RTSPanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(new Color(70, 150, 70));
        addMouseListener(this);
        addKeyListener(this);
        setFocusable(true);

        buildings.add(new Building(100, 100, "Town Center"));
        units.add(new Unit(180, 160, true));
        units.add(new Unit(220, 160, true));
        enemies.add(new Unit(760, 500, false));
        enemies.add(new Unit(720, 520, false));

        timer.start();
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        drawResources(g);

        for (int i = 0; i < 8; i++) {
            g.setColor(new Color(80, 60, 30));
            g.fillOval(520 + i * 30, 60 + (i % 2) * 30, 25, 25);
        }

        for (Building b : buildings) b.draw(g);
        for (Unit u : units) u.draw(g, u == selected);
        for (Unit e : enemies) e.draw(g, false);

        if (gameOver) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 42));
            g.drawString("GAME OVER", 330, 300);
        }
    }

    private void drawResources(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, WIDTH, 45);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("Wood: " + wood + "   Food: " + food, 20, 28);
        g.drawString("Left click select | Right click move/attack | V train villager | B build hut", 270, 28);
    }

    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            updateUnits();
            updateEnemies();
            spawnEnemies();
            checkCombat();
            if (units.isEmpty()) gameOver = true;
        }

        repaint();
    }

    private void updateUnits() {
        for (Unit u : units) u.move();
        for (Unit e : enemies) e.move();
    }

    private void updateEnemies() {
        for (Unit enemy : enemies) {
            Unit nearest = null;
            double best = 999999;

            for (Unit u : units) {
                double d = Math.hypot(enemy.x - u.x, enemy.y - u.y);
                if (d < best) {
                    best = d;
                    nearest = u;
                }
            }

            if (nearest != null && best < 250) {
                enemy.targetX = nearest.x;
                enemy.targetY = nearest.y;
            }
        }
    }

    private void spawnEnemies() {
        spawnTimer++;
        if (spawnTimer > 500) {
            enemies.add(new Unit(800, 500, false));
            spawnTimer = 0;
        }
    }

    private void checkCombat() {
        for (int i = units.size() - 1; i >= 0; i--) {
            Unit u = units.get(i);

            for (int j = enemies.size() - 1; j >= 0; j--) {
                Unit enemy = enemies.get(j);
                double d = Math.hypot(u.x - enemy.x, u.y - enemy.y);

                if (d < 28) {
                    u.health--;
                    enemy.health--;

                    if (enemy.health <= 0) {
                        enemies.remove(j);
                        food += 25;
                    }

                    if (u.health <= 0) {
                        units.remove(i);
                        break;
                    }
                }
            }
        }
    }

    public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            selected = null;
            for (Unit u : units) {
                if (Math.hypot(e.getX() - u.x, e.getY() - u.y) < 20) {
                    selected = u;
                    break;
                }
            }
        } else if (SwingUtilities.isRightMouseButton(e) && selected != null) {
            selected.targetX = e.getX();
            selected.targetY = e.getY();
        }
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_V && food >= 50) {
            units.add(new Unit(140, 160, true));
            food -= 50;
        }

        if (e.getKeyCode() == KeyEvent.VK_B && wood >= 75 && selected != null) {
            buildings.add(new Building((int)selected.x, (int)selected.y, "Hut"));
            wood -= 75;
        }
    }

    public void mouseReleased(MouseEvent e) {}
    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void keyReleased(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {}
}

class Unit {
    double x, y;
    double targetX, targetY;
    int health = 100;
    boolean friendly;
    double speed = 1.5;

    Unit(double x, double y, boolean friendly) {
        this.x = x;
        this.y = y;
        this.targetX = x;
        this.targetY = y;
        this.friendly = friendly;
    }

    void move() {
        double dx = targetX - x;
        double dy = targetY - y;
        double dist = Math.hypot(dx, dy);

        if (dist > speed) {
            x += speed * dx / dist;
            y += speed * dy / dist;
        }
    }

    void draw(Graphics g, boolean selected) {
        g.setColor(friendly ? Color.BLUE : Color.RED);
        g.fillOval((int)x - 14, (int)y - 14, 28, 28);

        g.setColor(Color.WHITE);
        g.drawString("" + health, (int)x - 13, (int)y - 20);

        if (selected) {
            g.setColor(Color.YELLOW);
            g.drawOval((int)x - 20, (int)y - 20, 40, 40);
        }
    }
}

class Building {
    int x, y;
    String type;

    Building(int x, int y, String type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    void draw(Graphics g) {
        g.setColor(type.equals("Town Center") ? Color.ORANGE : Color.GRAY);
        g.fillRect(x - 35, y - 35, 70, 70);
        g.setColor(Color.BLACK);
        g.drawRect(x - 35, y - 35, 70, 70);
        g.drawString(type, x - 35, y - 42);
    }
}
