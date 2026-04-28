import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Breakout extends JFrame {

    public Breakout() {
        add(new BreakoutPanel());
        setTitle("Breakout");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        new Breakout();
    }
}

class BreakoutPanel extends JPanel implements ActionListener, KeyListener {

    private final int WIDTH = 700;
    private final int HEIGHT = 600;

    private Timer timer;

    private int paddleX = 300;
    private final int paddleY = 540;
    private final int paddleWidth = 100;
    private final int paddleHeight = 15;
    private int paddleSpeed = 0;

    private int ballX = 340;
    private int ballY = 300;
    private int ballSize = 18;
    private int ballDX = 3;
    private int ballDY = -4;

    private final int rows = 5;
    private final int cols = 8;
    private final int brickWidth = 75;
    private final int brickHeight = 30;
    private boolean[][] bricks;

    private int score = 0;
    private boolean gameOver = false;
    private boolean gameWon = false;

    public BreakoutPanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        bricks = new boolean[rows][cols];
        resetBricks();

        timer = new Timer(10, this);
        timer.start();
    }

    private void resetBricks() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                bricks[r][c] = true;
            }
        }
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        drawPaddle(g);
        drawBall(g);
        drawBricks(g);
        drawScore(g);

        if (gameOver) {
            drawCenteredText(g, "Game Over - Press SPACE to Restart", HEIGHT / 2);
        }

        if (gameWon) {
            drawCenteredText(g, "You Win! - Press SPACE to Restart", HEIGHT / 2);
        }
    }

    private void drawPaddle(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillRect(paddleX, paddleY, paddleWidth, paddleHeight);
    }

    private void drawBall(Graphics g) {
        g.setColor(Color.RED);
        g.fillOval(ballX, ballY, ballSize, ballSize);
    }

    private void drawBricks(Graphics g) {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (bricks[r][c]) {
                    int x = 50 + c * brickWidth;
                    int y = 60 + r * brickHeight;

                    g.setColor(Color.ORANGE);
                    g.fillRect(x, y, brickWidth - 5, brickHeight - 5);

                    g.setColor(Color.BLACK);
                    g.drawRect(x, y, brickWidth - 5, brickHeight - 5);
                }
            }
        }
    }

    private void drawScore(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 22));
        g.drawString("Score: " + score, 20, 30);
    }

    private void drawCenteredText(Graphics g, String text, int y) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 28));
        FontMetrics metrics = getFontMetrics(g.getFont());
        int x = (WIDTH - metrics.stringWidth(text)) / 2;
        g.drawString(text, x, y);
    }

    public void actionPerformed(ActionEvent e) {
        if (!gameOver && !gameWon) {
            movePaddle();
            moveBall();
            checkCollisions();
        }

        repaint();
    }

    private void movePaddle() {
        paddleX += paddleSpeed;

        if (paddleX < 0) {
            paddleX = 0;
        }

        if (paddleX > WIDTH - paddleWidth) {
            paddleX = WIDTH - paddleWidth;
        }
    }

    private void moveBall() {
        ballX += ballDX;
        ballY += ballDY;

        if (ballX <= 0 || ballX >= WIDTH - ballSize) {
            ballDX *= -1;
        }

        if (ballY <= 0) {
            ballDY *= -1;
        }

        if (ballY > HEIGHT) {
            gameOver = true;
        }
    }

    private void checkCollisions() {
        Rectangle ballRect = new Rectangle(ballX, ballY, ballSize, ballSize);
        Rectangle paddleRect = new Rectangle(paddleX, paddleY, paddleWidth, paddleHeight);

        if (ballRect.intersects(paddleRect)) {
            ballDY = -Math.abs(ballDY);
        }

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (bricks[r][c]) {
                    int brickX = 50 + c * brickWidth;
                    int brickY = 60 + r * brickHeight;

                    Rectangle brickRect = new Rectangle(brickX, brickY, brickWidth - 5, brickHeight - 5);

                    if (ballRect.intersects(brickRect)) {
                        bricks[r][c] = false;
                        ballDY *= -1;
                        score += 10;
                        checkWin();
                        return;
                    }
                }
            }
        }
    }

    private void checkWin() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (bricks[r][c]) {
                    return;
                }
            }
        }

        gameWon = true;
    }

    private void restartGame() {
        paddleX = 300;
        paddleSpeed = 0;

        ballX = 340;
        ballY = 300;
        ballDX = 3;
        ballDY = -4;

        score = 0;
        gameOver = false;
        gameWon = false;

        resetBricks();
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            paddleSpeed = -7;
        }

        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            paddleSpeed = 7;
        }

        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (gameOver || gameWon) {
                restartGame();
            }
        }
    }

    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_RIGHT) {
            paddleSpeed = 0;
        }
    }

    public void keyTyped(KeyEvent e) {
    }
}