import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import javax.swing.*;

public class MinesweeperGame extends JFrame {

    public MinesweeperGame() {
        add(new MinePanel());
        setTitle("Minesweeper");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        new MinesweeperGame();
    }
}

class MinePanel extends JPanel {

    private final int ROWS = 12;
    private final int COLS = 12;
    private final int TILE_SIZE = 40;
    private final int MINES = 20;

    private Cell[][] board;
    private boolean gameOver;
    private boolean gameWon;

    public MinePanel() {
        setPreferredSize(new Dimension(COLS * TILE_SIZE, ROWS * TILE_SIZE + 50));
        setBackground(Color.DARK_GRAY);

        startGame();

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (gameOver || gameWon) {
                    startGame();
                    repaint();
                    return;
                }

                int col = e.getX() / TILE_SIZE;
                int row = e.getY() / TILE_SIZE;

                if (row >= ROWS || col >= COLS) {
                    return;
                }

                if (SwingUtilities.isRightMouseButton(e)) {
                    board[row][col].flagged = !board[row][col].flagged;
                } else {
                    reveal(row, col);
                }

                checkWin();
                repaint();
            }
        });
    }

    private void startGame() {
        board = new Cell[ROWS][COLS];
        gameOver = false;
        gameWon = false;

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                board[r][c] = new Cell();
            }
        }

        placeMines();
        calculateNumbers();
    }

    private void placeMines() {
        Random rand = new Random();
        int placed = 0;

        while (placed < MINES) {
            int r = rand.nextInt(ROWS);
            int c = rand.nextInt(COLS);

            if (!board[r][c].mine) {
                board[r][c].mine = true;
                placed++;
            }
        }
    }

    private void calculateNumbers() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (!board[r][c].mine) {
                    board[r][c].neighborMines = countNearbyMines(r, c);
                }
            }
        }
    }

    private int countNearbyMines(int row, int col) {
        int count = 0;

        for (int r = row - 1; r <= row + 1; r++) {
            for (int c = col - 1; c <= col + 1; c++) {
                if (inBounds(r, c) && board[r][c].mine) {
                    count++;
                }
            }
        }

        return count;
    }

    private void reveal(int row, int col) {
        if (!inBounds(row, col)) return;
        if (board[row][col].revealed || board[row][col].flagged) return;

        board[row][col].revealed = true;

        if (board[row][col].mine) {
            gameOver = true;
            revealAllMines();
            return;
        }

        if (board[row][col].neighborMines == 0) {
            for (int r = row - 1; r <= row + 1; r++) {
                for (int c = col - 1; c <= col + 1; c++) {
                    reveal(r, c);
                }
            }
        }
    }

    private void revealAllMines() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (board[r][c].mine) {
                    board[r][c].revealed = true;
                }
            }
        }
    }

    private void checkWin() {
        int revealedCount = 0;

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (board[r][c].revealed) {
                    revealedCount++;
                }
            }
        }

        if (revealedCount == ROWS * COLS - MINES) {
            gameWon = true;
        }
    }

    private boolean inBounds(int r, int c) {
        return r >= 0 && r < ROWS && c >= 0 && c < COLS;
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                drawCell(g, r, c);
            }
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));

        if (gameOver) {
            g.drawString("Game Over - Click to restart", 90, ROWS * TILE_SIZE + 32);
        } else if (gameWon) {
            g.drawString("You Win! - Click to restart", 100, ROWS * TILE_SIZE + 32);
        } else {
            g.drawString("Left click: reveal | Right click: flag", 60, ROWS * TILE_SIZE + 32);
        }
    }

    private void drawCell(Graphics g, int r, int c) {
        int x = c * TILE_SIZE;
        int y = r * TILE_SIZE;

        Cell cell = board[r][c];

        if (cell.revealed) {
            g.setColor(Color.LIGHT_GRAY);
        } else {
            g.setColor(Color.GRAY);
        }

        g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
        g.setColor(Color.BLACK);
        g.drawRect(x, y, TILE_SIZE, TILE_SIZE);

        if (cell.flagged && !cell.revealed) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString("F", x + 13, y + 28);
        }

        if (cell.revealed) {
            if (cell.mine) {
                g.setColor(Color.BLACK);
                g.fillOval(x + 10, y + 10, 20, 20);
            } else if (cell.neighborMines > 0) {
                g.setColor(Color.BLUE);
                g.setFont(new Font("Arial", Font.BOLD, 22));
                g.drawString(String.valueOf(cell.neighborMines), x + 14, y + 28);
            }
        }
    }
}

class Cell {
    boolean mine;
    boolean revealed;
    boolean flagged;
    int neighborMines;
}