import tkinter as tk

TILE = 80

class Chess:
    def __init__(self):
        self.root = tk.Tk()
        self.root.title("Simple Python Chess")
        self.canvas = tk.Canvas(self.root, width=640, height=700)
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

    def click(self, event):
        col, row = event.x // TILE, event.y // TILE
        if row < 0 or row >= 8 or col < 0 or col >= 8:
            return

        if self.selected is None:
            if self.board[row][col] and self.correct_turn(self.board[row][col]):
                self.selected = (row, col)
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

    def correct_turn(self, piece):
        return (self.white_turn and piece[0] == "w") or ((not self.white_turn) and piece[0] == "b")

    def valid_move(self, sr, sc, er, ec):
        piece = self.board[sr][sc]
        target = self.board[er][ec]

        if not piece:
            return False
        if target and target[0] == piece[0]:
            return False

        dr, dc = er - sr, ec - sc
        color, kind = piece[0], piece[1]

        if kind == "P":
            direction = -1 if color == "w" else 1
            start = 6 if color == "w" else 1

            if dc == 0 and dr == direction and not target:
                return True
            if dc == 0 and sr == start and dr == 2 * direction and not target and not self.board[sr + direction][sc]:
                return True
            if abs(dc) == 1 and dr == direction and target:
                return True

        if kind == "R":
            return (dr == 0 or dc == 0) and self.clear_path(sr, sc, er, ec)
        if kind == "B":
            return abs(dr) == abs(dc) and self.clear_path(sr, sc, er, ec)
        if kind == "Q":
            return (dr == 0 or dc == 0 or abs(dr) == abs(dc)) and self.clear_path(sr, sc, er, ec)
        if kind == "K":
            return abs(dr) <= 1 and abs(dc) <= 1
        if kind == "N":
            return (abs(dr), abs(dc)) in [(2, 1), (1, 2)]

        return False

    def clear_path(self, sr, sc, er, ec):
        step_r = (er > sr) - (er < sr)
        step_c = (ec > sc) - (ec < sc)

        r, c = sr + step_r, sc + step_c

        while (r, c) != (er, ec):
            if self.board[r][c]:
                return False
            r += step_r
            c += step_c

        return True

    def draw(self):
        self.canvas.delete("all")

        for r in range(8):
            for c in range(8):
                color = "#f0d9b5" if (r + c) % 2 == 0 else "#b58863"
                self.canvas.create_rectangle(c*TILE, r*TILE, c*TILE+TILE, r*TILE+TILE, fill=color)

                if self.selected == (r, c):
                    self.canvas.create_rectangle(c*TILE+4, r*TILE+4, c*TILE+TILE-4, r*TILE+TILE-4, outline="yellow", width=4)

                piece = self.board[r][c]
                if piece:
                    fill = "white" if piece[0] == "w" else "black"
                    self.canvas.create_text(c*TILE+40, r*TILE+43, text=piece[1], fill=fill, font=("Arial", 34, "bold"))

        self.canvas.create_rectangle(0, 640, 640, 700, fill="black")
        self.canvas.create_text(120, 670, text=self.message, fill="white", font=("Arial", 20, "bold"))

Chess()
