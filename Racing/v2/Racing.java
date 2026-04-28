import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import javax.swing.*;

public class Racing extends JFrame {
    public Racing() {
        add(new RacingPanel());
        setTitle("Need for Speed Style Racing");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        new Racing();
    }
}

class RacingPanel extends JPanel implements ActionListener, KeyListener {
    private final int WIDTH = 540, HEIGHT = 720;
    private Timer timer = new Timer(16, this);
    private int carX = 245, carY = 570, carW = 52, carH = 92;
    private int speed = 7, roadOffset = 0, score = 0;
    private boolean left, right, up, down, gameOver = false;
    private Random rand = new Random();
    private Rectangle[] traffic = {
        new Rectangle(130, -100, 52, 92),
        new Rectangle(245, -380, 52, 92),
        new Rectangle(360, -650, 52, 92)
    };

    public RacingPanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        addKeyListener(this);
        timer.start();
    }

    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawScene(g2);
        drawTraffic(g2);
        drawCar(g2, carX, carY, new Color(35, 85, 230), true);
        drawHUD(g2);

        if (gameOver) {
            g2.setColor(new Color(0,0,0,180));
            g2.fillRect(0, 295, WIDTH, 110);
            g2.setColor(Color.RED);
            g2.setFont(new Font("Arial", Font.BOLD, 44));
            g2.drawString("CRASH!", 190, 350);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 20));
            g2.drawString("Press SPACE to restart", 165, 385);
        }
    }

    private void drawScene(Graphics2D g2) {
        g2.setColor(new Color(20, 85, 35));
        g2.fillRect(0, 0, WIDTH, HEIGHT);

        g2.setColor(new Color(35, 35, 35));
        g2.fillRect(70, 0, 400, HEIGHT);

        g2.setColor(new Color(70,70,70));
        for (int y = -60 + roadOffset; y < HEIGHT; y += 80) {
            g2.fillRect(70, y, 400, 30);
        }

        g2.setColor(Color.WHITE);
        g2.fillRect(70, 0, 8, HEIGHT);
        g2.fillRect(462, 0, 8, HEIGHT);

        g2.setColor(Color.YELLOW);
        for (int y = -80 + roadOffset; y < HEIGHT; y += 130) {
            g2.fillRoundRect(266, y, 8, 75, 6, 6);
        }

        g2.setColor(new Color(15, 60, 25));
        for (int i = 0; i < 14; i++) {
            int x = i % 2 == 0 ? 20 : 485;
            int y = (i * 75 + roadOffset) % HEIGHT;
            g2.fillOval(x, y, 34, 26);
            g2.setColor(new Color(90,55,25));
            g2.fillRect(x+14, y+20, 6, 22);
            g2.setColor(new Color(15, 60, 25));
        }
    }

    private void drawTraffic(Graphics2D g2) {
        Color[] colors = {new Color(210,40,40), new Color(240,160,30), new Color(80,200,200)};
        for (int i = 0; i < traffic.length; i++) {
            Rectangle r = traffic[i];
            drawCar(g2, r.x, r.y, colors[i % colors.length], false);
        }
    }

    private void drawCar(Graphics2D g2, int x, int y, Color color, boolean player) {
        g2.setColor(new Color(0,0,0,90));
        g2.fillOval(x-5, y+carH-8, carW+10, 16);

        g2.setColor(color.darker());
        g2.fillRoundRect(x, y+10, carW, carH-18, 16, 16);
        g2.setColor(color);
        g2.fillRoundRect(x+5, y, carW-10, carH, 18, 18);

        g2.setColor(new Color(130,210,255));
        g2.fillRoundRect(x+10, y+12, carW-20, 18, 10, 10);
        g2.fillRoundRect(x+10, y+52, carW-20, 18, 10, 10);

        g2.setColor(Color.BLACK);
        g2.fillOval(x-5, y+18, 10, 22);
        g2.fillOval(x+carW-5, y+18, 10, 22);
        g2.fillOval(x-5, y+58, 10, 22);
        g2.fillOval(x+carW-5, y+58, 10, 22);

        if (player) {
            g2.setColor(new Color(255,120,0,180));
            g2.fillOval(x+15, y+carH-3, 8, 20);
            g2.fillOval(x+30, y+carH-3, 8, 20);
        }
    }

    private void drawHUD(Graphics2D g2) {
        g2.setColor(new Color(0,0,0,160));
        g2.fillRoundRect(12, 12, 516, 42, 15, 15);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 19));
        g2.drawString("Score: " + score, 25, 39);
        g2.drawString("Speed: " + speed, 395, 39);
        g2.drawString("Arrows to drive", 190, 39);
    }

    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            updateCar();
            updateRoad();
            updateTraffic();
            checkCollisions();
            score++;
        }
        repaint();
    }

    private void updateCar() {
        if (left) carX -= 6;
        if (right) carX += 6;
        if (up && speed < 15) speed++;
        if (down && speed > 4) speed--;
        if (carX < 88) carX = 88;
        if (carX > 400) carX = 400;
    }

    private void updateRoad() {
        roadOffset += speed;
        if (roadOffset >= 130) roadOffset = 0;
    }

    private void updateTraffic() {
        int[] lanes = {130, 245, 360};
        for (Rectangle r : traffic) {
            r.y += speed;
            if (r.y > HEIGHT) {
                r.y = -rand.nextInt(600) - 120;
                r.x = lanes[rand.nextInt(lanes.length)];
            }
        }
    }

    private void checkCollisions() {
        Rectangle player = new Rectangle(carX+5, carY+5, carW-10, carH-10);
        for (Rectangle r : traffic) {
            Rectangle hit = new Rectangle(r.x+5, r.y+5, r.width-10, r.height-10);
            if (player.intersects(hit)) gameOver = true;
        }
    }

    private void restart() {
        carX = 245; speed = 7; score = 0; gameOver = false;
        traffic[0].setLocation(130, -100);
        traffic[1].setLocation(245, -380);
        traffic[2].setLocation(360, -650);
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT) left = true;
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) right = true;
        if (e.getKeyCode() == KeyEvent.VK_UP) up = true;
        if (e.getKeyCode() == KeyEvent.VK_DOWN) down = true;
        if (e.getKeyCode() == KeyEvent.VK_SPACE && gameOver) restart();
    }

    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT) left = false;
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) right = false;
        if (e.getKeyCode() == KeyEvent.VK_UP) up = false;
        if (e.getKeyCode() == KeyEvent.VK_DOWN) down = false;
    }

    public void keyTyped(KeyEvent e) {}
}
