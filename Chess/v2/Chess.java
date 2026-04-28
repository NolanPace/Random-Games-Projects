import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Chess extends JFrame {
    public Chess() {
        add(new ChessPanel());
        setTitle("Visual Chess");
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
    private int selectedRow = -1, selectedCol = -1;
    private boolean whiteTurn = true;
    private String message = "White to move";

    public ChessPanel() {
        setPreferredSize(new Dimension(640, 720));
        addMouseListener(this);
        setupBoard();
    }

    private void setupBoard() {
        board[0] = new String[]{"bR","bN","bB","bQ","bK","bB","bN","bR"};
        board[1] = new String[]{"bP","bP","bP","bP","bP","bP","bP","bP"};
        for (int r = 2; r < 6; r++) for (int c = 0; c < 8; c++) board[r][c] = "";
        board[6] = new String[]{"wP","wP","wP","wP","wP","wP","wP","wP"};
        board[7] = new String[]{"wR","wN","wB","wQ","wK","wB","wN","wR"};
    }

    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Color base = (r + c) % 2 == 0 ? new Color(238, 218, 185) : new Color(118, 82, 48);
                g2.setPaint(new GradientPaint(c*TILE, r*TILE, base.brighter(), c*TILE+TILE, r*TILE+TILE, base.darker()));
                g2.fillRect(c*TILE, r*TILE, TILE, TILE);

                if (r == selectedRow && c == selectedCol) {
                    g2.setColor(new Color(255, 230, 0, 160));
                    g2.fillRect(c*TILE, r*TILE, TILE, TILE);
                }

                drawPiece(g2, board[r][c], c*TILE, r*TILE);
            }
        }

        g2.setColor(new Color(20, 20, 20));
        g2.fillRect(0, 640, 640, 80);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Serif", Font.BOLD, 24));
        g2.drawString(message, 20, 690);
    }

    private void drawPiece(Graphics2D g2, String piece, int x, int y) {
        if (piece.equals("")) return;

        boolean white = piece.charAt(0) == 'w';
        char type = piece.charAt(1);

        Color main = white ? new Color(245, 245, 235) : new Color(35, 35, 40);
        Color edge = white ? new Color(120, 120, 120) : new Color(230, 230, 230);

        g2.setColor(new Color(0, 0, 0, 80));
        g2.fillOval(x + 18, y + 58, 44, 12);

        g2.setColor(main);
        g2.fillOval(x + 20, y + 48, 40, 16);
        g2.fillRoundRect(x + 25, y + 28, 30, 30, 10, 10);

        if (type == 'P') {
            g2.fillOval(x + 27, y + 18, 26, 26);
        } else if (type == 'R') {
            g2.fillRect(x + 23, y + 18, 34, 16);
            g2.fillRect(x + 25, y + 12, 8, 10);
            g2.fillRect(x + 37, y + 12, 8, 10);
            g2.fillRect(x + 49, y + 12, 8, 10);
        } else if (type == 'N') {
            Polygon horse = new Polygon();
            horse.addPoint(x+25, y+52); horse.addPoint(x+31, y+20); horse.addPoint(x+43, y+12);
            horse.addPoint(x+56, y+25); horse.addPoint(x+50, y+31); horse.addPoint(x+58, y+48);
            horse.addPoint(x+45, y+54);
            g2.fillPolygon(horse);
            g2.setColor(edge);
            g2.fillOval(x+44, y+24, 4, 4);
        } else if (type == 'B') {
            g2.fillOval(x + 24, y + 12, 32, 35);
            g2.setColor(edge);
            g2.drawLine(x+40, y+15, x+34, y+38);
        } else if (type == 'Q') {
            g2.fillOval(x+24, y+20, 32, 28);
            for (int i = 0; i < 5; i++) {
                g2.setColor(main);
                g2.fillOval(x + 20 + i*9, y + 9 + (i%2)*3, 10, 10);
            }
        } else if (type == 'K') {
            g2.fillOval(x+24, y+20, 32, 28);
            g2.fillRect(x+37, y+8, 6, 20);
            g2.fillRect(x+30, y+14, 20, 5);
        }

        g2.setColor(edge);
        g2.setStroke(new BasicStroke(2));
        g2.drawOval(x + 20, y + 48, 40, 16);
        g2.drawRoundRect(x + 25, y + 28, 30, 30, 10, 10);
    }

    public void mousePressed(MouseEvent e) {
        int col = e.getX() / TILE;
        int row = e.getY() / TILE;
        if (row < 0 || row >= 8 || col < 0 || col >= 8) return;

        if (selectedRow == -1) {
            if (!board[row][col].equals("") && correctTurn(board[row][col])) {
                selectedRow = row; selectedCol = col;
                message = "Selected " + board[row][col];
            }
        } else {
            if (validMove(selectedRow, selectedCol, row, col)) {
                board[row][col] = board[selectedRow][selectedCol];
                board[selectedRow][selectedCol] = "";
                whiteTurn = !whiteTurn;
                message = whiteTurn ? "White to move" : "Black to move";
            } else message = "Illegal move";
            selectedRow = selectedCol = -1;
        }
        repaint();
    }

    private boolean correctTurn(String p) { return whiteTurn && p.charAt(0)=='w' || !whiteTurn && p.charAt(0)=='b'; }

    private boolean validMove(int sr, int sc, int er, int ec) {
        String p = board[sr][sc], t = board[er][ec];
        if (p.equals("") || (!t.equals("") && t.charAt(0) == p.charAt(0))) return false;
        int dr = er - sr, dc = ec - sc;
        char color = p.charAt(0), type = p.charAt(1);

        if (type == 'P') {
            int dir = color == 'w' ? -1 : 1, start = color == 'w' ? 6 : 1;
            if (dc == 0 && dr == dir && t.equals("")) return true;
            if (dc == 0 && sr == start && dr == 2*dir && t.equals("") && board[sr+dir][sc].equals("")) return true;
            if (Math.abs(dc) == 1 && dr == dir && !t.equals("")) return true;
        }
        if (type == 'R') return (dr == 0 || dc == 0) && clear(sr, sc, er, ec);
        if (type == 'B') return Math.abs(dr) == Math.abs(dc) && clear(sr, sc, er, ec);
        if (type == 'Q') return (dr == 0 || dc == 0 || Math.abs(dr) == Math.abs(dc)) && clear(sr, sc, er, ec);
        if (type == 'K') return Math.abs(dr) <= 1 && Math.abs(dc) <= 1;
        if (type == 'N') return Math.abs(dr) == 2 && Math.abs(dc) == 1 || Math.abs(dr) == 1 && Math.abs(dc) == 2;
        return false;
    }

    private boolean clear(int sr, int sc, int er, int ec) {
        int stepR = Integer.compare(er, sr), stepC = Integer.compare(ec, sc);
        int r = sr + stepR, c = sc + stepC;
        while (r != er || c != ec) {
            if (!board[r][c].equals("")) return false;
            r += stepR; c += stepC;
        }
        return true;
    }

    public void mouseReleased(MouseEvent e) {}
    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
}
