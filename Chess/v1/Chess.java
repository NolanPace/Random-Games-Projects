import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Chess extends JFrame {
    public Chess() {
        add(new ChessPanel());
        setTitle("Simple Java Chess");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        new Chess();
    }
}

class ChessPanel extends JPanel implements MouseListener {
    private final int TILE = 80;
    private String[][] board = new String[8][8];
    private int selectedRow = -1;
    private int selectedCol = -1;
    private boolean whiteTurn = true;
    private String message = "White to move";

    public ChessPanel() {
        setPreferredSize(new Dimension(640, 700));
        addMouseListener(this);
        setupBoard();
    }

    private void setupBoard() {
        board[0] = new String[]{"bR","bN","bB","bQ","bK","bB","bN","bR"};
        board[1] = new String[]{"bP","bP","bP","bP","bP","bP","bP","bP"};
        for (int r = 2; r < 6; r++) {
            for (int c = 0; c < 8; c++) board[r][c] = "";
        }
        board[6] = new String[]{"wP","wP","wP","wP","wP","wP","wP","wP"};
        board[7] = new String[]{"wR","wN","wB","wQ","wK","wB","wN","wR"};
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if ((r + c) % 2 == 0) g.setColor(new Color(240, 217, 181));
                else g.setColor(new Color(181, 136, 99));

                g.fillRect(c * TILE, r * TILE, TILE, TILE);

                if (r == selectedRow && c == selectedCol) {
                    g.setColor(Color.YELLOW);
                    g.drawRect(c * TILE + 3, r * TILE + 3, TILE - 6, TILE - 6);
                    g.drawRect(c * TILE + 4, r * TILE + 4, TILE - 8, TILE - 8);
                }

                drawPiece(g, board[r][c], c * TILE, r * TILE);
            }
        }

        g.setColor(Color.BLACK);
        g.fillRect(0, 640, 640, 60);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 22));
        g.drawString(message, 20, 675);
    }

    private void drawPiece(Graphics g, String p, int x, int y) {
        if (p.equals("")) return;

        String symbol = p.substring(1);
        g.setFont(new Font("Arial", Font.BOLD, 42));
        g.setColor(p.charAt(0) == 'w' ? Color.WHITE : Color.BLACK);
        g.drawString(symbol, x + 25, y + 52);
    }

    public void mousePressed(MouseEvent e) {
        int col = e.getX() / TILE;
        int row = e.getY() / TILE;

        if (row < 0 || row >= 8 || col < 0 || col >= 8) return;

        if (selectedRow == -1) {
            if (!board[row][col].equals("") && isCorrectTurn(board[row][col])) {
                selectedRow = row;
                selectedCol = col;
            }
        } else {
            if (isValidMove(selectedRow, selectedCol, row, col)) {
                board[row][col] = board[selectedRow][selectedCol];
                board[selectedRow][selectedCol] = "";
                whiteTurn = !whiteTurn;
                message = whiteTurn ? "White to move" : "Black to move";
            } else {
                message = "Illegal move";
            }

            selectedRow = -1;
            selectedCol = -1;
        }

        repaint();
    }

    private boolean isCorrectTurn(String piece) {
        return whiteTurn && piece.charAt(0) == 'w' || !whiteTurn && piece.charAt(0) == 'b';
    }

    private boolean isValidMove(int sr, int sc, int er, int ec) {
        String piece = board[sr][sc];
        if (piece.equals("")) return false;

        String target = board[er][ec];
        if (!target.equals("") && target.charAt(0) == piece.charAt(0)) return false;

        int dr = er - sr;
        int dc = ec - sc;
        char color = piece.charAt(0);
        char type = piece.charAt(1);

        if (type == 'P') {
            int dir = color == 'w' ? -1 : 1;
            int start = color == 'w' ? 6 : 1;

            if (dc == 0 && dr == dir && target.equals("")) return true;
            if (dc == 0 && sr == start && dr == 2 * dir && target.equals("") && board[sr + dir][sc].equals("")) return true;
            if (Math.abs(dc) == 1 && dr == dir && !target.equals("")) return true;
        }

        if (type == 'R') return (dr == 0 || dc == 0) && clearPath(sr, sc, er, ec);
        if (type == 'B') return Math.abs(dr) == Math.abs(dc) && clearPath(sr, sc, er, ec);
        if (type == 'Q') return (dr == 0 || dc == 0 || Math.abs(dr) == Math.abs(dc)) && clearPath(sr, sc, er, ec);
        if (type == 'K') return Math.abs(dr) <= 1 && Math.abs(dc) <= 1;
        if (type == 'N') return Math.abs(dr) == 2 && Math.abs(dc) == 1 || Math.abs(dr) == 1 && Math.abs(dc) == 2;

        return false;
    }

    private boolean clearPath(int sr, int sc, int er, int ec) {
        int stepR = Integer.compare(er, sr);
        int stepC = Integer.compare(ec, sc);

        int r = sr + stepR;
        int c = sc + stepC;

        while (r != er || c != ec) {
            if (!board[r][c].equals("")) return false;
            r += stepR;
            c += stepC;
        }

        return true;
    }

    public void mouseReleased(MouseEvent e) {}
    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
}
