import tkinter as tk

TILE = 40
ROWS, COLS = 12, 20
WIDTH, HEIGHT = COLS * TILE, ROWS * TILE

game_map = [
    [1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1],
    [1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,1],
    [1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1],
    [1,0,0,1,1,1,0,0,0,0,0,0,1,1,1,0,0,0,0,1],
    [1,0,0,1,0,0,0,0,0,3,0,0,0,0,1,0,0,0,0,1],
    [1,0,0,1,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,1],
    [1,0,0,1,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,1],
    [1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1],
    [1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1],
    [1,0,0,0,0,0,1,1,1,1,1,1,1,0,0,0,0,0,0,1],
    [1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1],
    [1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1],
]

class ZeldaRPG:
    def __init__(self):
        self.root = tk.Tk()
        self.root.title("Python Mini Zelda RPG")
        self.canvas = tk.Canvas(self.root, width=WIDTH, height=HEIGHT, bg="black")
        self.canvas.pack()

        self.player_x, self.player_y = 400, 300
        self.player_size = 30
        self.speed = 5
        self.health = 5
        self.coins = 0

        self.enemy_x, self.enemy_y = 200, 200
        self.enemy_health = 3

        self.keys = set()
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

    def hits_wall(self, x, y):
        corners = [
            (x, y),
            (x + self.player_size, y),
            (x, y + self.player_size),
            (x + self.player_size, y + self.player_size),
        ]

        for px, py in corners:
            row, col = py // TILE, px // TILE
            if row < 0 or row >= ROWS or col < 0 or col >= COLS:
                return True
            if game_map[row][col] == 1:
                return True

        return False

    def move(self):
        nx, ny = self.player_x, self.player_y

        if "w" in self.keys or "up" in self.keys:
            ny -= self.speed
        if "s" in self.keys or "down" in self.keys:
            ny += self.speed
        if "a" in self.keys or "left" in self.keys:
            nx -= self.speed
        if "d" in self.keys or "right" in self.keys:
            nx += self.speed

        if not self.hits_wall(nx, ny):
            self.player_x, self.player_y = nx, ny

    def check_tiles(self):
        row = (self.player_y + self.player_size // 2) // TILE
        col = (self.player_x + self.player_size // 2) // TILE

        if game_map[row][col] == 2:
            self.coins += 1
            game_map[row][col] = 0

        if game_map[row][col] == 3:
            self.win = True

    def rects_intersect(self, a, b):
        ax, ay, aw, ah = a
        bx, by, bw, bh = b
        return ax < bx + bw and ax + aw > bx and ay < by + bh and ay + ah > by

    def check_enemy(self):
        if self.enemy_health <= 0:
            return

        player = (self.player_x, self.player_y, self.player_size, self.player_size)
        enemy = (self.enemy_x, self.enemy_y, 30, 30)

        if self.rects_intersect(player, enemy):
            self.health -= 1
            self.player_x, self.player_y = 400, 300
            if self.health <= 0:
                self.game_over = True

    def attack(self):
        if self.enemy_health <= 0:
            return

        attack_box = (self.player_x - 20, self.player_y - 20, self.player_size + 40, self.player_size + 40)
        enemy = (self.enemy_x, self.enemy_y, 30, 30)

        if self.rects_intersect(attack_box, enemy):
            self.enemy_health -= 1

    def draw(self):
        self.canvas.delete("all")

        for r in range(ROWS):
            for c in range(COLS):
                x, y = c * TILE, r * TILE
                value = game_map[r][c]

                color = "forestgreen"
                if value == 1:
                    color = "saddlebrown"
                elif value == 2:
                    color = "gold"
                elif value == 3:
                    color = "cyan"

                self.canvas.create_rectangle(x, y, x + TILE, y + TILE, fill=color, outline="black")

        if self.enemy_health > 0:
            self.canvas.create_rectangle(self.enemy_x, self.enemy_y, self.enemy_x + 30, self.enemy_y + 30, fill="red")

        self.canvas.create_rectangle(
            self.player_x, self.player_y,
            self.player_x + self.player_size,
            self.player_y + self.player_size,
            fill="blue"
        )

        self.canvas.create_text(95, 20, text=f"Health: {self.health}   Coins: {self.coins}", fill="white", font=("Arial", 16, "bold"))
        self.canvas.create_text(470, 20, text="WASD/Arrows move | SPACE attack", fill="white", font=("Arial", 14, "bold"))

        if self.game_over:
            self.canvas.create_text(WIDTH // 2, HEIGHT // 2, text="GAME OVER", fill="white", font=("Arial", 42, "bold"))
        if self.win:
            self.canvas.create_text(WIDTH // 2, HEIGHT // 2, text="YOU WIN!", fill="white", font=("Arial", 42, "bold"))

    def loop(self):
        if not self.game_over and not self.win:
            self.move()
            self.check_tiles()
            self.check_enemy()

        self.draw()
        self.root.after(16, self.loop)

ZeldaRPG()
