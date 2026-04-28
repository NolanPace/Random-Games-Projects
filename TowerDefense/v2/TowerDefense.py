import tkinter as tk
import math

WIDTH, HEIGHT = 850, 620
PATH = [(0,310), (220,310), (220,130), (540,130), (540,440), (850,440)]

class Enemy:
    def __init__(self, wave):
        self.x, self.y = PATH[0]
        self.target = 1
        self.health = 3 + wave
        self.max_health = self.health
        self.speed = 1.25 + wave * 0.09
        self.finished = False

    def move(self):
        if self.target >= len(PATH):
            self.finished = True
            return
        tx, ty = PATH[self.target]
        dx, dy = tx - self.x, ty - self.y
        dist = math.hypot(dx, dy)
        if dist < self.speed:
            self.x, self.y = tx, ty
            self.target += 1
        else:
            self.x += self.speed * dx / dist
            self.y += self.speed * dy / dist

class Tower:
    def __init__(self, x, y):
        self.x = x
        self.y = y
        self.range = 125
        self.cooldown = 0

class Shot:
    def __init__(self, x, y, target):
        self.x = x
        self.y = y
        self.target = target
        self.speed = 9
        self.hit = False

    def move(self):
        dx, dy = self.target.x - self.x, self.target.y - self.y
        dist = math.hypot(dx, dy)
        if dist < self.speed:
            self.hit = True
        else:
            self.x += self.speed * dx / dist
            self.y += self.speed * dy / dist

class EnhancedTowerDefense:
    def __init__(self):
        self.root = tk.Tk()
        self.root.title("Enhanced Python Tower Defense")
        self.canvas = tk.Canvas(self.root, width=WIDTH, height=HEIGHT, bg="#19502d")
        self.canvas.pack()

        self.enemies = []
        self.towers = []
        self.shots = []
        self.money = 125
        self.lives = 12
        self.score = 0
        self.wave = 1
        self.spawn_timer = 0
        self.game_over = False

        self.canvas.bind("<Button-1>", self.place)
        self.loop()
        self.root.mainloop()

    def place(self, e):
        if not self.game_over and self.money >= 25:
            self.towers.append(Tower(e.x, e.y))
            self.money -= 25

    def update(self):
        if self.game_over:
            return
        self.spawn_timer += 1
        if self.spawn_timer > max(22, 82 - self.wave * 5):
            self.enemies.append(Enemy(self.wave))
            self.spawn_timer = 0

        for enemy in self.enemies[:]:
            enemy.move()
            if enemy.finished:
                self.lives -= 1
                self.enemies.remove(enemy)
            elif enemy.health <= 0:
                self.money += 10
                self.score += 10
                if self.score % 120 == 0:
                    self.wave += 1
                self.enemies.remove(enemy)

        for tower in self.towers:
            tower.cooldown -= 1
            if tower.cooldown <= 0:
                for enemy in self.enemies:
                    if math.hypot(tower.x-enemy.x, tower.y-enemy.y) <= tower.range:
                        self.shots.append(Shot(tower.x, tower.y, enemy))
                        tower.cooldown = 30
                        break

        for shot in self.shots[:]:
            shot.move()
            if shot.hit:
                shot.target.health -= 1
                self.shots.remove(shot)

        if self.lives <= 0:
            self.game_over = True

    def draw(self):
        self.canvas.delete("all")
        self.canvas.create_rectangle(0, 0, WIDTH, HEIGHT, fill="#19502d", outline="")
        for i in range(25):
            x = (i * 83) % WIDTH
            y = 65 + (i * 47) % 500
            self.canvas.create_oval(x, y, x+20, y+12, fill="#144123", outline="")

        for i in range(len(PATH)-1):
            self.canvas.create_line(PATH[i], PATH[i+1], fill="#5f462d", width=58, capstyle=tk.ROUND, joinstyle=tk.ROUND)
        for i in range(len(PATH)-1):
            self.canvas.create_line(PATH[i], PATH[i+1], fill="#916e46", width=40, capstyle=tk.ROUND, joinstyle=tk.ROUND)

        for tower in self.towers:
            self.canvas.create_oval(tower.x-tower.range, tower.y-tower.range, tower.x+tower.range, tower.y+tower.range, outline="#ffffff")
            self.canvas.create_oval(tower.x-22, tower.y-22, tower.x+22, tower.y+22, fill="#1e2855", outline="white")
            self.canvas.create_oval(tower.x-16, tower.y-16, tower.x+16, tower.y+16, fill="#4b6ed2", outline="")
            self.canvas.create_rectangle(tower.x-5, tower.y-34, tower.x+5, tower.y-2, fill="#333333", outline="")

        for enemy in self.enemies:
            self.canvas.create_oval(enemy.x-15, enemy.y+9, enemy.x+15, enemy.y+19, fill="#000000", outline="")
            self.canvas.create_oval(enemy.x-14, enemy.y-14, enemy.x+14, enemy.y+14, fill="#a52d2d", outline="")
            self.canvas.create_oval(enemy.x-7, enemy.y-8, enemy.x-2, enemy.y-3, fill="#ffa0a0", outline="")
            self.canvas.create_oval(enemy.x+3, enemy.y-8, enemy.x+8, enemy.y-3, fill="#ffa0a0", outline="")
            self.canvas.create_rectangle(enemy.x-18, enemy.y-25, enemy.x+18, enemy.y-20, fill="black", outline="")
            self.canvas.create_rectangle(enemy.x-18, enemy.y-25, enemy.x-18 + 36*max(0, enemy.health)/enemy.max_health, enemy.y-20, fill="green", outline="")

        for shot in self.shots:
            self.canvas.create_oval(shot.x-5, shot.y-5, shot.x+5, shot.y+5, fill="yellow", outline="white")

        self.canvas.create_rectangle(10, 10, 830, 52, fill="#000000", outline="#444444")
        self.canvas.create_text(330, 32, text=f"Money: ${self.money}   Lives: {self.lives}   Score: {self.score}   Wave: {self.wave}   Click to place cannon tower ($25)",
                                fill="white", font=("Arial", 16, "bold"))

        if self.game_over:
            self.canvas.create_rectangle(0, 250, WIDTH, 350, fill="#000000")
            self.canvas.create_text(WIDTH//2, 310, text="GAME OVER", fill="red", font=("Arial", 42, "bold"))

    def loop(self):
        self.update()
        self.draw()
        self.root.after(16, self.loop)

EnhancedTowerDefense()
