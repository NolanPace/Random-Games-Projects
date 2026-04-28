import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import javax.swing.*;

public class Racing extends JFrame {
    public Racing() {
        add(new RacingPanel());
        setTitle("Need for Speed Style Java Racing");
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
    private final int WIDTH = 500;
    private final int HEIGHT = 700;
    private Timer timer = new Timer(16, this);

    private int carX = 225;
    private int carY = 560;
    private int carW = 50;
    private int carH = 90;

    private int speed = 6;
    private int roadOffset = 0;
    private int score = 0;

    private boolean left, right, up, down;
    private boolean gameOver = false;

    private Random rand = new Random();
    private Rectangle[] traffic = {
            new Rectangle(120, -100, 50, 90),
            new Rectangle(330, -350, 50, 90),
            new Rectangle(220, -600, 50, 90)
    };

    public RacingPanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        addKeyListener(this);
        setFocusable(true);
        timer.start();
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        drawRoad(g);
        drawTraffic(g);
        drawPlayer(g);
        drawHUD(g);

        if (gameOver) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 36));
            g.drawString("CRASH!", 185, 330);
            g.setFont(new Font("Arial", Font.BOLD, 20));
            g.drawString("Press SPACE to restart", 145, 365);
        }
    }

    private void drawRoad(Graphics g) {
        g.setColor(Color.GRAY);
        g.fillRect(70, 0, 360, HEIGHT);

        g.setColor(Color.WHITE);
        g.fillRect(70, 0, 8, HEIGHT);
        g.fillRect(422, 0, 8, HEIGHT);

        for (int y = -80 + roadOffset; y < HEIGHT; y += 120) {
            g.fillRect(245, y, 10, 70);
        }
    }

    private void drawPlayer(Graphics g) {
        g.setColor(Color.BLUE);
        g.fillRect(carX, carY, carW, carH);
        g.setColor(Color.CYAN);
        g.fillRect(carX + 8, carY + 10, carW - 16, 18);
    }

    private void drawTraffic(Graphics g) {
        g.setColor(Color.RED);
        for (Rectangle r : traffic) {
            g.fillRect(r.x, r.y, r.width, r.height);
        }
    }

    private void drawHUD(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Score: " + score, 20, 30);
        g.drawString("Speed: " + speed, 370, 30);
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
        if (up && speed < 14) speed++;
        if (down && speed > 4) speed--;

        if (carX < 80) carX = 80;
        if (carX > 370) carX = 370;
    }

    private void updateRoad() {
        roadOffset += speed;
        if (roadOffset >= 120) roadOffset = 0;
    }

    private void updateTraffic() {
        for (Rectangle r : traffic) {
            r.y += speed;

            if (r.y > HEIGHT) {
                r.y = -rand.nextInt(500) - 100;
                int[] lanes = {120, 225, 330};
                r.x = lanes[rand.nextInt(lanes.length)];
            }
        }
    }

    private void checkCollisions() {
        Rectangle player = new Rectangle(carX, carY, carW, carH);

        for (Rectangle r : traffic) {
            if (player.intersects(r)) {
                gameOver = true;
            }
        }
    }

    private void restart() {
        carX = 225;
        speed = 6;
        score = 0;
        gameOver = false;

        traffic[0].setLocation(120, -100);
        traffic[1].setLocation(330, -350);
        traffic[2].setLocation(220, -600);
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT) left = true;
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) right = true;
        if (e.getKeyCode() == KeyEvent.VK_UP) up = true;
        if (e.getKeyCode() == KeyEvent.VK_DOWN) down = true;

        if (e.getKeyCode() == KeyEvent.VK_SPACE && gameOver) {
            restart();
        }
    }

    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT) left = false;
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) right = false;
        if (e.getKeyCode() == KeyEvent.VK_UP) up = false;
        if (e.getKeyCode() == KeyEvent.VK_DOWN) down = false;
    }

    public void keyTyped(KeyEvent e) {}
}
