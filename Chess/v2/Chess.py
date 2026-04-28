import tkinter as tk

TILE = 80

class EnhancedChess:
    def __init__(self):
        self.root = tk.Tk()
        self.root.title("Enhanced Visual Python Chess")
        self.canvas = tk.Canvas(self.root, width=640, height=720)
        self.canvas.pack()

        self.board = [
            ["bR","bN","bB","bQ","bK","bB","bN","bR"],
            ["bP","bP","bP","bP","bP","bP","bP","bP"],
            ["","","","","","","",""],
            ["","","","","","","",""],
            ["","","","","","","",""],
            ["","","","","","","",""],
            ["wP","wP","wP","wP","wP","wP","wP","wP"],
            ["wR","wN","wB","wQ","wK","wB","wN","wR"]
        ]
        self.selected = None
        self.white_turn = True
        self.message = "White to move"

        self.canvas.bind("<Button-1>", self.click)
        self.draw()
        self.root.mainloop()

    def draw_piece(self, piece, x, y):
        if not piece:
            return
        white = piece[0] == "w"
        kind = piece[1]
        main = "#f5f5eb" if white else "#232328"
        edge = "#777777" if white else "#e6e6e6"

        self.canvas.create_oval(x+18, y+58, x+62, y+70, fill="#000000", outline="")
        self.canvas.create_oval(x+20, y+48, x+60, y+64, fill=main, outline=edge, width=2)
        self.canvas.create_rectangle(x+25, y+28, x+55, y+58, fill=main, outline=edge, width=2)

        if kind == "P":
            self.canvas.create_oval(x+27, y+18, x+53, y+44, fill=main, outline=edge, width=2)
        elif kind == "R":
            self.canvas.create_rectangle(x+23, y+18, x+57, y+34, fill=main, outline=edge, width=2)
            for sx in [25, 37, 49]:
                self.canvas.create_rectangle(x+sx, y+12, x+sx+8, y+22, fill=main, outline=edge)
        elif kind == "N":
            pts = [x+25,y+52,x+31,y+20,x+43,y+12,x+56,y+25,x+50,y+31,x+58,y+48,x+45,y+54]
            self.canvas.create_polygon(pts, fill=main, outline=edge, width=2)
            self.canvas.create_oval(x+44, y+24, x+48, y+28, fill=edge, outline="")
        elif kind == "B":
            self.canvas.create_oval(x+24, y+12, x+56, y+47, fill=main, outline=edge, width=2)
            self.canvas.create_line(x+40, y+15, x+34, y+38, fill=edge, width=2)
        elif kind == "Q":
            self.canvas.create_oval(x+24, y+20, x+56, y+48, fill=main, outline=edge, width=2)
            for i in range(5):
                self.canvas.create_oval(x+20+i*9, y+9+(i%2)*3, x+30+i*9, y+19+(i%2)*3, fill=main, outline=edge)
        elif kind == "K":
            self.canvas.create_oval(x+24, y+20, x+56, y+48, fill=main, outline=edge, width=2)
            self.canvas.create_rectangle(x+37, y+8, x+43, y+28, fill=main, outline=edge)
            self.canvas.create_rectangle(x+30, y+14, x+50, y+19, fill=main, outline=edge)

    def draw(self):
        self.canvas.delete("all")
        for r in range(8):
            for c in range(8):
                color = "#eedab9" if (r+c)%2 == 0 else "#765230"
                self.canvas.create_rectangle(c*TILE, r*TILE, c*TILE+TILE, r*TILE+TILE, fill=color, outline="")
                if self.selected == (r, c):
                    self.canvas.create_rectangle(c*TILE+3, r*TILE+3, c*TILE+TILE-3, r*TILE+TILE-3, outline="yellow", width=5)
                self.draw_piece(self.board[r][c], c*TILE, r*TILE)

        self.canvas.create_rectangle(0, 640, 640, 720, fill="#141414")
        self.canvas.create_text(120, 682, text=self.message, fill="white", font=("Georgia", 22, "bold"))

    def click(self, e):
        row, col = e.y // TILE, e.x // TILE
        if not (0 <= row < 8 and 0 <= col < 8):
            return

        if self.selected is None:
            if self.board[row][col] and self.correct_turn(self.board[row][col]):
                self.selected = (row, col)
                self.message = "Selected " + self.board[row][col]
        else:
            sr, sc = self.selected
            if self.valid_move(sr, sc, row, col):
                self.board[row][col] = self.board[sr][sc]
                self.board[sr][sc] = ""
                self.white_turn = not self.white_turn
                self.message = "White to move" if self.white_turn else "Black to move"
            else:
                self.message = "Illegal move"
            self.selected = None
        self.draw()

    def correct_turn(self, p):
        return (self.white_turn and p[0]=="w") or ((not self.white_turn) and p[0]=="b")

    def valid_move(self, sr, sc, er, ec):
        p = self.board[sr][sc]
        t = self.board[er][ec]
        if not p or (t and t[0] == p[0]):
            return False
        dr, dc = er-sr, ec-sc
        color, kind = p[0], p[1]
        if kind == "P":
            direction = -1 if color == "w" else 1
            start = 6 if color == "w" else 1
            if dc == 0 and dr == direction and not t: return True
            if dc == 0 and sr == start and dr == 2*direction and not t and not self.board[sr+direction][sc]: return True
            if abs(dc) == 1 and dr == direction and t: return True
        if kind == "R": return (dr == 0 or dc == 0) and self.clear(sr, sc, er, ec)
        if kind == "B": return abs(dr) == abs(dc) and self.clear(sr, sc, er, ec)
        if kind == "Q": return (dr == 0 or dc == 0 or abs(dr) == abs(dc)) and self.clear(sr, sc, er, ec)
        if kind == "K": return abs(dr) <= 1 and abs(dc) <= 1
        if kind == "N": return (abs(dr), abs(dc)) in [(2,1),(1,2)]
        return False

    def clear(self, sr, sc, er, ec):
        step_r = (er > sr) - (er < sr)
        step_c = (ec > sc) - (ec < sc)
        r, c = sr + step_r, sc + step_c
        while (r, c) != (er, ec):
            if self.board[r][c]:
                return False
            r += step_r
            c += step_c
        return True

EnhancedChess()
