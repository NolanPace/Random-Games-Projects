import tkinter as tk
import random

WIDTH, HEIGHT = 500, 700

class Racing:
    def __init__(self):
        self.root = tk.Tk()
        self.root.title("Need for Speed Style Python Racing")
        self.canvas = tk.Canvas(self.root, width=WIDTH, height=HEIGHT, bg="black")
        self.canvas.pack()

        self.car_x = 225
        self.car_y = 560
        self.car_w = 50
        self.car_h = 90

        self.speed = 6
        self.road_offset = 0
        self.score = 0
        self.game_over = False

        self.keys = set()
        self.traffic = [
            [120, -100, 50, 90],
            [330, -350, 50, 90],
            [225, -600, 50, 90]
        ]

        self.root.bind("<KeyPress>", self.key_press)
        self.root.bind("<KeyRelease>", self.key_release)

        self.loop()
        self.root.mainloop()

    def key_press(self, e):
        self.keys.add(e.keysym.lower())

        if e.keysym == "space" and self.game_over:
            self.restart()

    def key_release(self, e):
        self.keys.discard(e.keysym.lower())

    def update(self):
        if self.game_over:
            return

        if "left" in self.keys:
            self.car_x -= 6
        if "right" in self.keys:
            self.car_x += 6
        if "up" in self.keys and self.speed < 14:
            self.speed += 1
        if "down" in self.keys and self.speed > 4:
            self.speed -= 1

        self.car_x = max(80, min(370, self.car_x))

        self.road_offset += self.speed
        if self.road_offset >= 120:
            self.road_offset = 0

        for car in self.traffic:
            car[1] += self.speed
            if car[1] > HEIGHT:
                car[1] = -random.randint(100, 600)
                car[0] = random.choice([120, 225, 330])

        self.check_collisions()
        self.score += 1

    def intersects(self, a, b):
        ax, ay, aw, ah = a
        bx, by, bw, bh = b
        return ax < bx + bw and ax + aw > bx and ay < by + bh and ay + ah > by

    def check_collisions(self):
        player = [self.car_x, self.car_y, self.car_w, self.car_h]

        for car in self.traffic:
            if self.intersects(player, car):
                self.game_over = True

    def restart(self):
        self.car_x = 225
        self.speed = 6
        self.score = 0
        self.game_over = False
        self.traffic = [
            [120, -100, 50, 90],
            [330, -350, 50, 90],
            [225, -600, 50, 90]
        ]

    def draw(self):
        self.canvas.delete("all")

        self.canvas.create_rectangle(70, 0, 430, HEIGHT, fill="gray")
        self.canvas.create_rectangle(70, 0, 78, HEIGHT, fill="white")
        self.canvas.create_rectangle(422, 0, 430, HEIGHT, fill="white")

        for y in range(-80 + self.road_offset, HEIGHT, 120):
            self.canvas.create_rectangle(245, y, 255, y + 70, fill="white")

        for car in self.traffic:
            x, y, w, h = car
            self.canvas.create_rectangle(x, y, x + w, y + h, fill="red")

        self.canvas.create_rectangle(self.car_x, self.car_y, self.car_x + self.car_w, self.car_y + self.car_h, fill="blue")
        self.canvas.create_rectangle(self.car_x + 8, self.car_y + 10, self.car_x + self.car_w - 8, self.car_y + 28, fill="cyan")

        self.canvas.create_text(70, 30, text=f"Score: {self.score}", fill="white", font=("Arial", 18, "bold"))
        self.canvas.create_text(420, 30, text=f"Speed: {self.speed}", fill="white", font=("Arial", 18, "bold"))

        if self.game_over:
            self.canvas.create_text(WIDTH // 2, HEIGHT // 2, text="CRASH!", fill="white", font=("Arial", 40, "bold"))
            self.canvas.create_text(WIDTH // 2, HEIGHT // 2 + 40, text="Press SPACE to restart", fill="white", font=("Arial", 18, "bold"))

    def loop(self):
        self.update()
        self.draw()
        self.root.after(16, self.loop)

Racing()
