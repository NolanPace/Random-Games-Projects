import tkinter as tk
import math

TILE = 40
ROWS, COLS = 12, 20
WIDTH, HEIGHT = COLS * TILE, ROWS * TILE

game_map = [
    [1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1],
    [1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,1],
    [1,0,4,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,1],
    [1,0,0,1,1,1,0,0,0,0,0,0,1,1,1,0,0,4,0,1],
    [1,0,0,1,0,0,0,4,0,3,0,0,0,0,1,0,0,0,0,1],
    [1,0,0,1,0,0,0,0,0,0,0,0,0,0,1,0,4,0,0,1],
    [1,0,0,1,0,4,0,0,0,0,0,0,4,0,1,0,0,0,0,1],
    [1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1],
    [1,0,4,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,1],
    [1,0,0,0,0,0,1,1,1,1,1,1,1,0,0,0,0,4,0,1],
    [1,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,1],
    [1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1],
]

class EnhancedZeldaRPG:
    def __init__(self):
        self.root = tk.Tk()
        self.root.title("Enhanced Python Zelda-Style RPG")
        self.canvas = tk.Canvas(self.root, width=WIDTH, height=HEIGHT, bg="black")
        self.canvas.pack()

        self.player_x = 400
        self.player_y = 300
        self.size = 30
        self.health = 5
        self.coins = 0
        self.enemy_x = 210
        self.enemy_y = 210
        self.enemy_health = 4
        self.keys = set()
        self.anim = 0
        self.game_over = False
        self.win = False

        self.root.bind("<KeyPress>", self.key_press)
        self.root.bind("<KeyRelease>", self.key_release)

        self.loop()
        self.root.mainloop()

    def key_press(self, e):
        self.keys.add(e.keysym.lower())
        if e.keysym == "space":
            self.attack()

    def key_release(self, e):
        self.keys.discard(e.keysym.lower())

    def draw_world(self):
        self.canvas.create_rectangle(0, 0, WIDTH, HEIGHT, fill="#1e5f28", outline="")

        for r in range(ROWS):
            for c in range(COLS):
                x, y = c * TILE, r * TILE
                val = game_map[r][c]

                if val == 1:
                    self.canvas.create_rectangle(x, y, x+TILE, y+TILE, fill="#503219", outline="#2d1b0d")
                    self.canvas.create_rectangle(x+5, y+5, x+TILE-5, y+TILE-5, fill="#785028", outline="")
                else:
                    self.canvas.create_rectangle(x, y, x+TILE, y+TILE, fill="#378737", outline="#2f7730")
                    self.canvas.create_oval(x+7, y+8, x+13, y+14, fill="#46a046", outline="")
                    self.canvas.create_oval(x+24, y+25, x+30, y+31, fill="#46a046", outline="")

                if val == 2:
                    self.draw_coin(x+20, y+20)
                elif val == 3:
                    self.draw_portal(x+20, y+20)
                elif val == 4:
                    self.draw_tree(x, y)

    def draw_coin(self, cx, cy):
        self.canvas.create_oval(cx-10, cy-10, cx+10, cy+10, fill="#ffd200", outline="#785000", width=2)
        self.canvas.create_oval(cx-5, cy-6, cx+2, cy+1, fill="#fff58c", outline="")

    def draw_portal(self, cx, cy):
        pulse = int(math.sin(self.anim * 0.1) * 5)
        self.canvas.create_oval(cx-17-pulse, cy-17-pulse, cx+17+pulse, cy+17+pulse, fill="#00ffff", outline="")
        self.canvas.create_oval(cx-13, cy-13, cx+13, cy+13, fill="#0050b4", outline="white", width=2)

    def draw_tree(self, x, y):
        self.canvas.create_rectangle(x+17, y+20, x+24, y+38, fill="#502d14", outline="")
        self.canvas.create_oval(x+7, y+5, x+33, y+31, fill="#196423", outline="")
        self.canvas.create_oval(x+13, y, x+33, y+20, fill="#28872d", outline="")

    def draw_player(self):
        x, y = self.player_x, self.player_y
        self.canvas.create_oval(x-3, y+24, x+36, y+36, fill="#111111", outline="")
        self.canvas.create_rectangle(x, y+8, x+self.size, y+self.size+4, fill="#285ae6", outline="#102e91")
        self.canvas.create_oval(x+5, y, x+25, y+20, fill="#f5cda0", outline="#8a5a35")
        self.canvas.create_polygon(x+6, y+4, x+15, y-13, x+24, y+4, fill="#143ca0")
        self.canvas.create_oval(x+9, y+8, x+13, y+12, fill="white")
        self.canvas.create_oval(x+18, y+8, x+22, y+12, fill="white")
        self.canvas.create_rectangle(x+28, y+14, x+46, y+19, fill="#d0d0d0", outline="white")

    def draw_enemy(self):
        if self.enemy_health <= 0:
            return
        x, y = self.enemy_x, self.enemy_y
        self.canvas.create_oval(x-3, y+24, x+36, y+36, fill="#111111", outline="")
        self.canvas.create_oval(x, y, x+32, y+32, fill="#aa141e", outline="#500000")
        self.canvas.create_oval(x+6, y+9, x+12, y+15, fill="#500000")
        self.canvas.create_oval(x+20, y+9, x+26, y+15, fill="#500000")
        self.canvas.create_text(x+16, y-8, text=f"HP {self.enemy_health}", fill="white", font=("Arial", 10, "bold"))

    def draw_hud(self):
        self.canvas.create_rectangle(10, 10, 465, 48, fill="#000000", outline="#444444")
        for i in range(5):
            color = "red" if i < self.health else "#333333"
            self.canvas.create_oval(25+i*28, 18, 45+i*28, 38, fill=color, outline="")
        self.canvas.create_text(215, 30, text=f"Coins: {self.coins}", fill="white", font=("Arial", 16, "bold"))
        self.canvas.create_text(360, 30, text="Move: WASD/Arrows  Attack: SPACE", fill="white", font=("Arial", 12, "bold"))

    def draw(self):
        self.canvas.delete("all")
        self.draw_world()
        self.draw_enemy()
        self.draw_player()
        self.draw_hud()

        if self.game_over or self.win:
            self.canvas.create_rectangle(0, 250, WIDTH, 350, fill="#000000")
            text = "GAME OVER" if self.game_over else "YOU FOUND THE PORTAL!"
            color = "red" if self.game_over else "cyan"
            self.canvas.create_text(WIDTH//2, 310, text=text, fill=color, font=("Arial", 36, "bold"))

    def hits_wall(self, x, y):
        corners = [(x, y), (x+self.size, y), (x, y+self.size), (x+self.size, y+self.size)]
        for px, py in corners:
            r, c = py // TILE, px // TILE
            if r < 0 or r >= ROWS or c < 0 or c >= COLS:
                return True
            if game_map[r][c] == 1:
                return True
        return False

    def move(self):
        nx, ny = self.player_x, self.player_y
        if "w" in self.keys or "up" in self.keys: ny -= 5
        if "s" in self.keys or "down" in self.keys: ny += 5
        if "a" in self.keys or "left" in self.keys: nx -= 5
        if "d" in self.keys or "right" in self.keys: nx += 5
        if not self.hits_wall(nx, ny):
            self.player_x, self.player_y = nx, ny

    def intersects(self, a, b):
        ax, ay, aw, ah = a
        bx, by, bw, bh = b
        return ax < bx+bw and ax+aw > bx and ay < by+bh and ay+ah > by

    def check_tiles_enemy(self):
        r = (self.player_y + self.size//2) // TILE
        c = (self.player_x + self.size//2) // TILE
        if game_map[r][c] == 2:
            self.coins += 1
            game_map[r][c] = 0
        if game_map[r][c] == 3:
            self.win = True

        if self.enemy_health > 0 and self.intersects(
            (self.player_x, self.player_y, self.size, self.size),
            (self.enemy_x, self.enemy_y, 32, 32)
        ):
            self.health -= 1
            self.player_x, self.player_y = 400, 300
            if self.health <= 0:
                self.game_over = True

    def attack(self):
        if self.enemy_health > 0 and self.intersects(
            (self.player_x-25, self.player_y-25, self.size+50, self.size+50),
            (self.enemy_x, self.enemy_y, 32, 32)
        ):
            self.enemy_health -= 1

    def loop(self):
        self.anim += 1
        if not self.game_over and not self.win:
            self.move()
            self.check_tiles_enemy()
        self.draw()
        self.root.after(16, self.loop)

EnhancedZeldaRPG()
