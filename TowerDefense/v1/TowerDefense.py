import tkinter as tk
import math

WIDTH, HEIGHT = 800, 600
PATH = [(0, 300), (200, 300), (200, 120), (500, 120), (500, 420), (800, 420)]

class Enemy:
    def __init__(self, wave):
        self.x, self.y = PATH[0]
        self.target_index = 1
        self.health = 3 + wave
        self.speed = 1.2 + wave * 0.08
        self.finished = False

    def move(self):
        if self.target_index >= len(PATH):
            self.finished = True
            return

        tx, ty = PATH[self.target_index]
        dx, dy = tx - self.x, ty - self.y
        dist = math.hypot(dx, dy)

        if dist < self.speed:
            self.x, self.y = tx, ty
            self.target_index += 1
        else:
            self.x += self.speed * dx / dist
            self.y += self.speed * dy / dist

class Tower:
    def __init__(self, x, y):
        self.x = x
        self.y = y
        self.range = 120
        self.cooldown = 0

class Shot:
    def __init__(self, x, y, target):
        self.x = x
        self.y = y
        self.target = target
        self.speed = 8
        self.hit = False

    def move(self):
        dx, dy = self.target.x - self.x, self.target.y - self.y
        dist = math.hypot(dx, dy)

        if dist < self.speed:
            self.hit = True
        else:
            self.x += self.speed * dx / dist
            self.y += self.speed * dy / dist

class TowerDefense:
    def __init__(self):
        self.root = tk.Tk()
        self.root.title("Python Tower Defense")
        self.canvas = tk.Canvas(self.root, width=WIDTH, height=HEIGHT, bg="black")
        self.canvas.pack()

        self.enemies = []
        self.towers = []
        self.shots = []

        self.money = 100
        self.lives = 10
        self.score = 0
        self.wave = 1
        self.spawn_timer = 0
        self.game_over = False

        self.canvas.bind("<Button-1>", self.place_tower)

        self.loop()
        self.root.mainloop()

    def place_tower(self, e):
        if self.money >= 25 and not self.game_over:
            self.towers.append(Tower(e.x, e.y))
            self.money -= 25

    def spawn(self):
        self.spawn_timer += 1
        if self.spawn_timer > max(25, 90 - self.wave * 5):
            self.enemies.append(Enemy(self.wave))
            self.spawn_timer = 0

    def update(self):
        if self.game_over:
            return

        self.spawn()

        for enemy in self.enemies[:]:
            enemy.move()
            if enemy.finished:
                self.lives -= 1
                self.enemies.remove(enemy)
            elif enemy.health <= 0:
                self.score += 10
                self.money += 10
                self.enemies.remove(enemy)
                if self.score % 100 == 0:
                    self.wave += 1

        for tower in self.towers:
            tower.cooldown -= 1
            if tower.cooldown <= 0:
                target = None
                for enemy in self.enemies:
                    if math.hypot(tower.x - enemy.x, tower.y - enemy.y) <= tower.range:
                        target = enemy
                        break

                if target:
                    self.shots.append(Shot(tower.x, tower.y, target))
                    tower.cooldown = 35

        for shot in self.shots[:]:
            shot.move()
            if shot.hit:
                shot.target.health -= 1
                self.shots.remove(shot)

        if self.lives <= 0:
            self.game_over = True

    def draw(self):
        self.canvas.delete("all")

        for i in range(len(PATH) - 1):
            self.canvas.create_line(PATH[i], PATH[i+1], fill="gray25", width=45)

        for tower in self.towers:
            self.canvas.create_rectangle(tower.x - 15, tower.y - 15, tower.x + 15, tower.y + 15, fill="blue")
            self.canvas.create_oval(tower.x - tower.range, tower.y - tower.range,
                                    tower.x + tower.range, tower.y + tower.range, outline="gray40")

        for enemy in self.enemies:
            self.canvas.create_oval(enemy.x - 12, enemy.y - 12, enemy.x + 12, enemy.y + 12, fill="red")
            self.canvas.create_text(enemy.x, enemy.y, text=str(enemy.health), fill="white")

        for shot in self.shots:
            self.canvas.create_oval(shot.x - 4, shot.y - 4, shot.x + 4, shot.y + 4, fill="yellow")

        self.canvas.create_text(240, 20, text=f"Money: ${self.money}   Lives: {self.lives}   Score: {self.score}   Click to place tower ($25)",
                                fill="white", font=("Arial", 16, "bold"))

        if self.game_over:
            self.canvas.create_text(WIDTH // 2, HEIGHT // 2, text="GAME OVER", fill="white", font=("Arial", 42, "bold"))

    def loop(self):
        self.update()
        self.draw()
        self.root.after(16, self.loop)

TowerDefense()
