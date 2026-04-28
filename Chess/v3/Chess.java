import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public class Chess extends JFrame {
    public Chess() {
        add(new ChessPanel());
        setTitle("Chess vs Computer - Difficulty Options");
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

class ChessPanel extends JPanel implements MouseListener, KeyListener {
    private final int TILE = 80;
    private String[][] board = new String[8][8];
    private int selectedRow = -1, selectedCol = -1;
    private boolean whiteTurn = true;
    private boolean vsComputer = true;
    private int difficulty = 2; // 1 easy, 2 medium, 3 hard
    private String message = "White to move | C toggle AI | 1/2/3 difficulty";
    private Random rand = new Random();

    public ChessPanel() {
        setPreferredSize(new Dimension(640, 740));
        addMouseListener(this);
        addKeyListener(this);
        setFocusable(true);
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
                Color base = (r+c)%2==0 ? new Color(238,218,185) : new Color(118,82,48);
                g2.setPaint(new GradientPaint(c*TILE, r*TILE, base.brighter(), c*TILE+TILE, r*TILE+TILE, base.darker()));
                g2.fillRect(c*TILE, r*TILE, TILE, TILE);
                if (r == selectedRow && c == selectedCol) {
                    g2.setColor(new Color(255,230,0,160));
                    g2.fillRect(c*TILE, r*TILE, TILE, TILE);
                }
                drawPiece(g2, board[r][c], c*TILE, r*TILE);
            }
        }

        g2.setColor(new Color(20,20,20));
        g2.fillRect(0,640,640,100);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 18));
        g2.drawString(message, 15, 675);
        g2.drawString("Mode: " + (vsComputer ? "vs Computer" : "2 Player") +
                " | Difficulty: " + diffName(), 15, 708);
    }

    private String diffName() {
        if (difficulty == 1) return "Easy";
        if (difficulty == 2) return "Medium";
        return "Hard";
    }

    private void drawPiece(Graphics2D g2, String piece, int x, int y) {
        if (piece.equals("")) return;
        boolean white = piece.charAt(0) == 'w';
        char type = piece.charAt(1);

        Color main = white ? new Color(245,245,235) : new Color(35,35,40);
        Color edge = white ? new Color(120,120,120) : new Color(230,230,230);

        g2.setColor(new Color(0,0,0,80));
        g2.fillOval(x+18,y+58,44,12);
        g2.setColor(main);
        g2.fillOval(x+20,y+48,40,16);
        g2.fillRoundRect(x+25,y+28,30,30,10,10);

        if (type == 'P') {
            g2.fillOval(x+27,y+18,26,26);
        } else if (type == 'R') {
            g2.fillRect(x+23,y+18,34,16);
            g2.fillRect(x+25,y+12,8,10);
            g2.fillRect(x+37,y+12,8,10);
            g2.fillRect(x+49,y+12,8,10);
        } else if (type == 'N') {
            Polygon horse = new Polygon();
            horse.addPoint(x+25,y+52); horse.addPoint(x+31,y+20); horse.addPoint(x+43,y+12);
            horse.addPoint(x+56,y+25); horse.addPoint(x+50,y+31); horse.addPoint(x+58,y+48);
            horse.addPoint(x+45,y+54);
            g2.fillPolygon(horse);
            g2.setColor(edge);
            g2.fillOval(x+44,y+24,4,4);
        } else if (type == 'B') {
            g2.fillOval(x+24,y+12,32,35);
            g2.setColor(edge);
            g2.drawLine(x+40,y+15,x+34,y+38);
        } else if (type == 'Q') {
            g2.fillOval(x+24,y+20,32,28);
            for (int i=0; i<5; i++) {
                g2.setColor(main);
                g2.fillOval(x+20+i*9,y+9+(i%2)*3,10,10);
            }
        } else if (type == 'K') {
            g2.fillOval(x+24,y+20,32,28);
            g2.fillRect(x+37,y+8,6,20);
            g2.fillRect(x+30,y+14,20,5);
        }

        g2.setColor(edge);
        g2.setStroke(new BasicStroke(2));
        g2.drawOval(x+20,y+48,40,16);
        g2.drawRoundRect(x+25,y+28,30,30,10,10);
    }

    public void mousePressed(MouseEvent e) {
        requestFocusInWindow();

        if (vsComputer && !whiteTurn) return;

        int col = e.getX() / TILE;
        int row = e.getY() / TILE;
        if (row < 0 || row >= 8 || col < 0 || col >= 8) return;

        if (selectedRow == -1) {
            if (!board[row][col].equals("") && correctTurn(board[row][col])) {
                selectedRow = row;
                selectedCol = col;
                message = "Selected " + board[row][col];
            }
        } else {
            if (validMove(board, selectedRow, selectedCol, row, col)) {
                movePiece(selectedRow, selectedCol, row, col);
                selectedRow = selectedCol = -1;

                if (vsComputer && !whiteTurn) {
                    message = "Computer thinking...";
                    repaint();
                    javax.swing.Timer t = new javax.swing.Timer(350, ev -> {
                        computerMove();
                        ((javax.swing.Timer)ev.getSource()).stop();
                    });
                    t.setRepeats(false);
                    t.start();
                }
            } else {
                message = "Illegal move";
                selectedRow = selectedCol = -1;
            }
        }
        repaint();
    }

    private void movePiece(int sr, int sc, int er, int ec) {
        board[er][ec] = board[sr][sc];
        board[sr][sc] = "";
        whiteTurn = !whiteTurn;
        message = whiteTurn ? "White to move" : "Black to move";
    }

    private boolean correctTurn(String p) {
        return whiteTurn && p.charAt(0)=='w' || !whiteTurn && p.charAt(0)=='b';
    }

    private ArrayList<Move> allMoves(String color) {
        ArrayList<Move> moves = new ArrayList<>();
        for (int sr=0; sr<8; sr++) {
            for (int sc=0; sc<8; sc++) {
                if (!board[sr][sc].equals("") && board[sr][sc].startsWith(color)) {
                    for (int er=0; er<8; er++) {
                        for (int ec=0; ec<8; ec++) {
                            if (validMove(board, sr, sc, er, ec)) {
                                moves.add(new Move(sr, sc, er, ec));
                            }
                        }
                    }
                }
            }
        }
        return moves;
    }

    private void computerMove() {
        ArrayList<Move> moves = allMoves("b");
        if (moves.isEmpty()) {
            message = "Computer has no legal moves";
            return;
        }

        Move best;
        if (difficulty == 1) {
            best = moves.get(rand.nextInt(moves.size()));
        } else if (difficulty == 2) {
            best = bestGreedyMove(moves);
        } else {
            best = bestMinimaxMove(moves);
        }

        movePiece(best.sr, best.sc, best.er, best.ec);
        message = "Computer moved | White to move";
        repaint();
    }

    private Move bestGreedyMove(ArrayList<Move> moves) {
        Move best = moves.get(0);
        int bestScore = -9999;
        for (Move m : moves) {
            String target = board[m.er][m.ec];
            int score = target.equals("") ? 0 : value(target.charAt(1));
            score += rand.nextInt(3);
            if (score > bestScore) {
                bestScore = score;
                best = m;
            }
        }
        return best;
    }

    private Move bestMinimaxMove(ArrayList<Move> moves) {
        Move best = moves.get(0);
        int bestScore = -999999;

        for (Move m : moves) {
            String captured = board[m.er][m.ec];
            String piece = board[m.sr][m.sc];

            board[m.er][m.ec] = piece;
            board[m.sr][m.sc] = "";

            int score = evaluateBoard() - bestWhiteReplyScore();

            board[m.sr][m.sc] = piece;
            board[m.er][m.ec] = captured;

            if (score > bestScore) {
                bestScore = score;
                best = m;
            }
        }

        return best;
    }

    private int bestWhiteReplyScore() {
        ArrayList<Move> replies = allMovesForBoard("w", board);
        int best = 0;
        for (Move m : replies) {
            String target = board[m.er][m.ec];
            int score = target.equals("") ? 0 : value(target.charAt(1));
            best = Math.max(best, score);
        }
        return best;
    }

    private ArrayList<Move> allMovesForBoard(String color, String[][] b) {
        ArrayList<Move> moves = new ArrayList<>();
        for (int sr=0; sr<8; sr++) {
            for (int sc=0; sc<8; sc++) {
                if (!b[sr][sc].equals("") && b[sr][sc].startsWith(color)) {
                    for (int er=0; er<8; er++) {
                        for (int ec=0; ec<8; ec++) {
                            if (validMove(b, sr, sc, er, ec)) moves.add(new Move(sr, sc, er, ec));
                        }
                    }
                }
            }
        }
        return moves;
    }

    private int evaluateBoard() {
        int score = 0;
        for (int r=0; r<8; r++) {
            for (int c=0; c<8; c++) {
                String p = board[r][c];
                if (!p.equals("")) {
                    int v = value(p.charAt(1));
                    score += p.charAt(0) == 'b' ? v : -v;
                }
            }
        }
        return score;
    }

    private int value(char type) {
        if (type == 'P') return 10;
        if (type == 'N' || type == 'B') return 30;
        if (type == 'R') return 50;
        if (type == 'Q') return 90;
        if (type == 'K') return 900;
        return 0;
    }

    private boolean validMove(String[][] b, int sr, int sc, int er, int ec) {
        if (sr == er && sc == ec) return false;
        String p = b[sr][sc], t = b[er][ec];
        if (p.equals("") || (!t.equals("") && t.charAt(0) == p.charAt(0))) return false;

        int dr = er - sr, dc = ec - sc;
        char color = p.charAt(0), type = p.charAt(1);

        if (type == 'P') {
            int dir = color == 'w' ? -1 : 1;
            int start = color == 'w' ? 6 : 1;
            if (dc == 0 && dr == dir && t.equals("")) return true;
            if (dc == 0 && sr == start && dr == 2*dir && t.equals("") && b[sr+dir][sc].equals("")) return true;
            if (Math.abs(dc) == 1 && dr == dir && !t.equals("")) return true;
        }
        if (type == 'R') return (dr == 0 || dc == 0) && clear(b, sr, sc, er, ec);
        if (type == 'B') return Math.abs(dr) == Math.abs(dc) && clear(b, sr, sc, er, ec);
        if (type == 'Q') return (dr == 0 || dc == 0 || Math.abs(dr) == Math.abs(dc)) && clear(b, sr, sc, er, ec);
        if (type == 'K') return Math.abs(dr) <= 1 && Math.abs(dc) <= 1;
        if (type == 'N') return Math.abs(dr) == 2 && Math.abs(dc) == 1 || Math.abs(dr) == 1 && Math.abs(dc) == 2;
        return false;
    }

    private boolean clear(String[][] b, int sr, int sc, int er, int ec) {
        int stepR = Integer.compare(er, sr), stepC = Integer.compare(ec, sc);
        int r = sr + stepR, c = sc + stepC;
        while (r != er || c != ec) {
            if (!b[r][c].equals("")) return false;
            r += stepR;
            c += stepC;
        }
        return true;
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_C) {
            vsComputer = !vsComputer;
            message = vsComputer ? "Computer opponent ON" : "Two-player mode ON";
        }
        if (e.getKeyCode() == KeyEvent.VK_1) difficulty = 1;
        if (e.getKeyCode() == KeyEvent.VK_2) difficulty = 2;
        if (e.getKeyCode() == KeyEvent.VK_3) difficulty = 3;
        repaint();
    }

    public void mouseReleased(MouseEvent e) {}
    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void keyReleased(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {}
}

class Move {
    int sr, sc, er, ec;
    Move(int sr, int sc, int er, int ec) {
        this.sr = sr; this.sc = sc; this.er = er; this.ec = ec;
    }
}
