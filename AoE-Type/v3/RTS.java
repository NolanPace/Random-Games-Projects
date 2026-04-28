import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;

public class RTS extends JFrame {
    public RTS() {
        add(new RTSPanel());
        setTitle("RTS with Computer Difficulty");
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

    private ArrayList<AIUnit> playerUnits = new ArrayList<>();
    private ArrayList<AIUnit> enemyUnits = new ArrayList<>();
    private ArrayList<AIBuilding> buildings = new ArrayList<>();
    private AIUnit selected = null;

    private int wood = 250, food = 150;
    private int enemyDifficulty = 2;
    private int enemySpawnTimer = 0;
    private int enemyDecisionTimer = 0;
    private boolean gameOver = false;

    public RTSPanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        addMouseListener(this);
        addKeyListener(this);

        buildings.add(new AIBuilding(110, 115, "Town Center"));
        playerUnits.add(new AIUnit(190,170,true));
        playerUnits.add(new AIUnit(230,170,true));
        enemyUnits.add(new AIUnit(790,510,false));
        enemyUnits.add(new AIUnit(750,535,false));

        timer.start();
    }

    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setPaint(new GradientPaint(0,0,new Color(75,155,75),0,HEIGHT,new Color(40,100,60)));
        g2.fillRect(0,0,WIDTH,HEIGHT);

        drawMap(g2);
        for (AIBuilding b : buildings) b.draw(g2);
        for (AIUnit u : playerUnits) u.draw(g2, u == selected);
        for (AIUnit e : enemyUnits) e.draw(g2, false);
        drawHUD(g2);

        if (gameOver) {
            g2.setColor(new Color(0,0,0,180));
            g2.fillRect(0, 280, WIDTH, 100);
            g2.setColor(Color.RED);
            g2.setFont(new Font("Arial", Font.BOLD, 44));
            g2.drawString("GAME OVER", 340, 345);
        }
    }

    private void drawMap(Graphics2D g2) {
        for (int i = 0; i < 10; i++) {
            int x = 540 + i*31;
            int y = 70 + (i%2)*28;
            g2.setColor(new Color(80,50,25));
            g2.fillOval(x,y,26,18);
            g2.setColor(new Color(35,100,35));
            g2.fillOval(x-5,y-18,36,30);
        }

        g2.setColor(new Color(60,90,150));
        g2.fillRoundRect(620,500,260,55,35,35);
        g2.setColor(new Color(140,190,230,120));
        g2.fillRoundRect(640,510,210,20,20,20);
    }

    private void drawHUD(Graphics2D g2) {
        g2.setColor(new Color(0,0,0,175));
        g2.fillRoundRect(10,10,930,45,14,14);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 15));
        g2.drawString("Wood: " + wood + " Food: " + food, 25, 38);
        g2.drawString("Left click select | Right click move/attack | V train | B build | 1/2/3 AI Difficulty: " + diffName(), 240, 38);
    }

    private String diffName() {
        if (enemyDifficulty == 1) return "Easy";
        if (enemyDifficulty == 2) return "Medium";
        return "Hard";
    }

    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            for (AIUnit u : playerUnits) u.move();
            for (AIUnit eUnit : enemyUnits) eUnit.move();

            enemyAI();
            combat();

            if (playerUnits.isEmpty()) gameOver = true;
        }
        repaint();
    }

    private void enemyAI() {
        enemySpawnTimer++;
        enemyDecisionTimer++;

        int spawnRate = enemyDifficulty == 1 ? 750 : enemyDifficulty == 2 ? 520 : 350;
        int decisionRate = enemyDifficulty == 1 ? 150 : enemyDifficulty == 2 ? 80 : 35;
        double aggroRange = enemyDifficulty == 1 ? 170 : enemyDifficulty == 2 ? 280 : 420;

        if (enemySpawnTimer > spawnRate) {
            enemyUnits.add(new AIUnit(820, 520, false));
            enemySpawnTimer = 0;
        }

        if (enemyDecisionTimer > decisionRate) {
            for (AIUnit enemy : enemyUnits) {
                AIUnit nearest = null;
                double best = 999999;

                for (AIUnit u : playerUnits) {
                    double d = Math.hypot(enemy.x-u.x, enemy.y-u.y);
                    if (d < best) {
                        best = d;
                        nearest = u;
                    }
                }

                if (nearest != null) {
                    if (enemyDifficulty == 1 && best < aggroRange) {
                        enemy.targetX = nearest.x;
                        enemy.targetY = nearest.y;
                    } else if (enemyDifficulty == 2 && best < aggroRange) {
                        enemy.targetX = nearest.x;
                        enemy.targetY = nearest.y;
                    } else if (enemyDifficulty == 3) {
                        enemy.targetX = nearest.x;
                        enemy.targetY = nearest.y;
                    }
                }
            }
            enemyDecisionTimer = 0;
        }
    }

    private void combat() {
        int enemyDamage = enemyDifficulty == 1 ? 1 : enemyDifficulty == 2 ? 2 : 3;

        for (int i = playerUnits.size()-1; i >= 0; i--) {
            AIUnit u = playerUnits.get(i);
            for (int j = enemyUnits.size()-1; j >= 0; j--) {
                AIUnit enemy = enemyUnits.get(j);
                if (Math.hypot(u.x-enemy.x, u.y-enemy.y) < 28) {
                    u.health -= enemyDamage;
                    enemy.health -= 2;

                    if (enemy.health <= 0) {
                        enemyUnits.remove(j);
                        food += 25;
                    }

                    if (u.health <= 0) {
                        playerUnits.remove(i);
                        break;
                    }
                }
            }
        }
    }

    public void mousePressed(MouseEvent e) {
        requestFocusInWindow();

        if (SwingUtilities.isLeftMouseButton(e)) {
            selected = null;
            for (AIUnit u : playerUnits) {
                if (Math.hypot(e.getX()-u.x, e.getY()-u.y) < 24) {
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
        if (e.getKeyCode() == KeyEvent.VK_1) enemyDifficulty = 1;
        if (e.getKeyCode() == KeyEvent.VK_2) enemyDifficulty = 2;
        if (e.getKeyCode() == KeyEvent.VK_3) enemyDifficulty = 3;

        if (e.getKeyCode() == KeyEvent.VK_V && food >= 50) {
            playerUnits.add(new AIUnit(155,170,true));
            food -= 50;
        }

        if (e.getKeyCode() == KeyEvent.VK_B && wood >= 75 && selected != null) {
            buildings.add(new AIBuilding((int)selected.x, (int)selected.y, "Hut"));
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

class AIUnit {
    double x, y, targetX, targetY;
    int health = 100;
    boolean friendly;
    double speed = 1.5;

    AIUnit(double x, double y, boolean friendly) {
        this.x = x; this.y = y; this.targetX = x; this.targetY = y; this.friendly = friendly;
    }

    void move() {
        double dx = targetX-x, dy = targetY-y;
        double dist = Math.hypot(dx,dy);
        if (dist > speed) {
            x += speed*dx/dist;
            y += speed*dy/dist;
        }
    }

    void draw(Graphics2D g2, boolean selected) {
        g2.setColor(new Color(0,0,0,80));
        g2.fillOval((int)x-15,(int)y+10,30,10);
        g2.setColor(friendly ? new Color(40,80,210) : new Color(180,40,35));
        g2.fillOval((int)x-12,(int)y-18,24,24);
        g2.fillRoundRect((int)x-10,(int)y,20,28,8,8);
        g2.setColor(new Color(230,190,140));
        g2.fillOval((int)x-8,(int)y-26,16,16);
        g2.setColor(Color.WHITE);
        g2.drawString("" + health, (int)x-15, (int)y-32);
        if (selected) {
            g2.setColor(Color.YELLOW);
            g2.setStroke(new BasicStroke(3));
            g2.drawOval((int)x-24,(int)y-30,48,62);
            g2.setStroke(new BasicStroke(1));
        }
    }
}

class AIBuilding {
    int x, y;
    String type;

    AIBuilding(int x, int y, String type) {
        this.x = x; this.y = y; this.type = type;
    }

    void draw(Graphics2D g2) {
        if (type.equals("Town Center")) {
            g2.setColor(new Color(120,75,35));
            g2.fillRect(x-50,y-30,100,70);
            g2.setColor(new Color(150,40,25));
            g2.fillPolygon(new int[]{x-60,x,x+60}, new int[]{y-30,y-75,y-30}, 3);
        } else {
            g2.setColor(new Color(115,80,55));
            g2.fillRect(x-30,y-25,60,50);
            g2.setColor(new Color(90,45,25));
            g2.fillPolygon(new int[]{x-35,x,x+35}, new int[]{y-25,y-55,y-25}, 3);
        }
        g2.setColor(Color.BLACK);
        g2.drawString(type, x-40, y+55);
    }
}
