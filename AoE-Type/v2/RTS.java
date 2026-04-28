import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class RTS extends JFrame {
    public RTS() {
        add(new RTSPanel());
        setTitle("Age of Empires Style RTS");
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
    private final int WIDTH = 950, HEIGHT = 680;
    private Timer timer = new Timer(16, this);
    private ArrayList<RTSUnit> units = new ArrayList<>();
    private ArrayList<RTSUnit> enemies = new ArrayList<>();
    private ArrayList<RTSBuilding> buildings = new ArrayList<>();
    private RTSUnit selected = null;
    private int wood = 250, food = 150, spawnTimer = 0;
    private boolean gameOver = false;

    public RTSPanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        addMouseListener(this);
        addKeyListener(this);

        buildings.add(new RTSBuilding(110, 115, "Town Center"));
        units.add(new RTSUnit(190, 170, true));
        units.add(new RTSUnit(230, 170, true));
        enemies.add(new RTSUnit(790, 510, false));
        enemies.add(new RTSUnit(750, 535, false));

        timer.start();
    }

    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setPaint(new GradientPaint(0,0,new Color(75,155,75),0,HEIGHT,new Color(40,100,60)));
        g2.fillRect(0, 0, WIDTH, HEIGHT);

        drawMapDetails(g2);
        for (RTSBuilding b : buildings) b.draw(g2);
        for (RTSUnit u : units) u.draw(g2, u == selected);
        for (RTSUnit e : enemies) e.draw(g2, false);
        drawHUD(g2);

        if (gameOver) {
            g2.setColor(new Color(0,0,0,180));
            g2.fillRect(0, 280, WIDTH, 100);
            g2.setColor(Color.RED);
            g2.setFont(new Font("Arial", Font.BOLD, 46));
            g2.drawString("YOUR VILLAGE HAS FALLEN", 170, 345);
        }
    }

    private void drawMapDetails(Graphics2D g2) {
        for (int i = 0; i < 10; i++) {
            int x = 540 + i * 31;
            int y = 70 + (i % 2) * 28;
            g2.setColor(new Color(80,50,25));
            g2.fillOval(x, y, 26, 18);
            g2.setColor(new Color(35,100,35));
            g2.fillOval(x-5, y-18, 36, 30);
        }

        g2.setColor(new Color(60,90,150));
        g2.fillRoundRect(620, 500, 260, 55, 35, 35);
        g2.setColor(new Color(140,190,230,120));
        g2.fillRoundRect(640, 510, 210, 20, 20, 20);
    }

    private void drawHUD(Graphics2D g2) {
        g2.setColor(new Color(0,0,0,170));
        g2.fillRoundRect(10, 10, 930, 42, 14, 14);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 17));
        g2.drawString("Wood: " + wood + "   Food: " + food, 25, 37);
        g2.drawString("Left click select | Right click move/attack | V train villager | B build hut", 280, 37);
    }

    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            for (RTSUnit u : units) u.move();
            for (RTSUnit eUnit : enemies) eUnit.move();

            for (RTSUnit enemy : enemies) {
                RTSUnit nearest = null;
                double best = 99999;
                for (RTSUnit u : units) {
                    double d = Math.hypot(enemy.x-u.x, enemy.y-u.y);
                    if (d < best) { best = d; nearest = u; }
                }
                if (nearest != null && best < 275) {
                    enemy.targetX = nearest.x; enemy.targetY = nearest.y;
                }
            }

            for (int i = units.size()-1; i >= 0; i--) {
                RTSUnit u = units.get(i);
                for (int j = enemies.size()-1; j >= 0; j--) {
                    RTSUnit enemy = enemies.get(j);
                    if (Math.hypot(u.x-enemy.x, u.y-enemy.y) < 28) {
                        u.health--; enemy.health--;
                        if (enemy.health <= 0) { enemies.remove(j); food += 25; }
                        if (u.health <= 0) { units.remove(i); break; }
                    }
                }
            }

            spawnTimer++;
            if (spawnTimer > 520) {
                enemies.add(new RTSUnit(820, 520, false));
                spawnTimer = 0;
            }

            if (units.isEmpty()) gameOver = true;
        }
        repaint();
    }

    public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            selected = null;
            for (RTSUnit u : units) {
                if (Math.hypot(e.getX()-u.x, e.getY()-u.y) < 22) { selected = u; break; }
            }
        } else if (SwingUtilities.isRightMouseButton(e) && selected != null) {
            selected.targetX = e.getX();
            selected.targetY = e.getY();
        }
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_V && food >= 50) {
            units.add(new RTSUnit(155, 170, true));
            food -= 50;
        }
        if (e.getKeyCode() == KeyEvent.VK_B && wood >= 75 && selected != null) {
            buildings.add(new RTSBuilding((int)selected.x, (int)selected.y, "Hut"));
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

class RTSUnit {
    double x, y, targetX, targetY;
    int health = 100;
    boolean friendly;
    double speed = 1.5;

    RTSUnit(double x, double y, boolean friendly) {
        this.x = x; this.y = y; this.targetX = x; this.targetY = y; this.friendly = friendly;
    }

    void move() {
        double dx = targetX-x, dy = targetY-y;
        double dist = Math.hypot(dx, dy);
        if (dist > speed) { x += speed*dx/dist; y += speed*dy/dist; }
    }

    void draw(Graphics2D g2, boolean selected) {
        g2.setColor(new Color(0,0,0,80));
        g2.fillOval((int)x-15, (int)y+10, 30, 10);
        g2.setColor(friendly ? new Color(40,80,210) : new Color(180,40,35));
        g2.fillOval((int)x-12, (int)y-18, 24, 24);
        g2.fillRoundRect((int)x-10, (int)y, 20, 28, 8, 8);
        g2.setColor(new Color(230,190,140));
        g2.fillOval((int)x-8, (int)y-26, 16, 16);
        g2.setColor(Color.WHITE);
        g2.drawString("" + health, (int)x-15, (int)y-32);
        if (selected) {
            g2.setColor(Color.YELLOW);
            g2.setStroke(new BasicStroke(3));
            g2.drawOval((int)x-24, (int)y-30, 48, 62);
            g2.setStroke(new BasicStroke(1));
        }
    }
}

class RTSBuilding {
    int x, y;
    String type;

    RTSBuilding(int x, int y, String type) { this.x = x; this.y = y; this.type = type; }

    void draw(Graphics2D g2) {
        if (type.equals("Town Center")) {
            g2.setColor(new Color(120,75,35));
            g2.fillRect(x-50, y-30, 100, 70);
            g2.setColor(new Color(150,40,25));
            Polygon roof = new Polygon(new int[]{x-60,x,x+60}, new int[]{y-30,y-75,y-30}, 3);
            g2.fillPolygon(roof);
        } else {
            g2.setColor(new Color(115,80,55));
            g2.fillRect(x-30, y-25, 60, 50);
            g2.setColor(new Color(90,45,25));
            g2.fillPolygon(new int[]{x-35,x,x+35}, new int[]{y-25,y-55,y-25}, 3);
        }
        g2.setColor(Color.BLACK);
        g2.drawString(type, x-40, y+55);
    }
}
