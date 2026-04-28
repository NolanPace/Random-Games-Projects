import tkinter as tk
import random

WIDTH, HEIGHT = 540, 720

class EnhancedRacing:
    def __init__(self):
        self.root = tk.Tk()
        self.root.title("Enhanced Python Need for Speed Style Racing")
        self.canvas = tk.Canvas(self.root, width=WIDTH, height=HEIGHT, bg="black")
        self.canvas.pack()

        self.car_x = 245
        self.car_y = 570
        self.car_w = 52
        self.car_h = 92
        self.speed = 7
        self.road_offset = 0
        self.score = 0
        self.game_over = False
        self.keys = set()
        self.traffic = [
            [130, -100, 52, 92, "#d22828"],
            [245, -380, 52, 92, "#f0a01e"],
            [360, -650, 52, 92, "#50c8c8"]
        ]

        self.root.bind("<KeyPress>", self.press)
        self.root.bind("<KeyRelease>", self.release)

        self.loop()
        self.root.mainloop()

    def press(self, e):
        self.keys.add(e.keysym.lower())
        if e.keysym == "space" and self.game_over:
            self.restart()

    def release(self, e):
        self.keys.discard(e.keysym.lower())

    def draw_car(self, x, y, color, player=False):
        self.canvas.create_oval(x-5, y+self.car_h-8, x+self.car_w+5, y+self.car_h+8, fill="#000000", outline="")
        self.canvas.create_rectangle(x, y+10, x+self.car_w, y+self.car_h-8, fill=color, outline="")
        self.canvas.create_rectangle(x+5, y, x+self.car_w-5, y+self.car_h, fill=color, outline="white")
        self.canvas.create_rectangle(x+10, y+12, x+self.car_w-10, y+30, fill="#82d2ff", outline="")
        self.canvas.create_rectangle(x+10, y+52, x+self.car_w-10, y+70, fill="#82d2ff", outline="")
        for wx, wy in [(x-5,y+18),(x+self.car_w-5,y+18),(x-5,y+58),(x+self.car_w-5,y+58)]:
            self.canvas.create_oval(wx, wy, wx+10, wy+22, fill="black", outline="")
        if player:
            self.canvas.create_oval(x+15, y+self.car_h-3, x+23, y+self.car_h+17, fill="#ff7800", outline="")
            self.canvas.create_oval(x+30, y+self.car_h-3, x+38, y+self.car_h+17, fill="#ff7800", outline="")

    def update(self):
        if self.game_over:
            return

        if "left" in self.keys:
            self.car_x -= 6
        if "right" in self.keys:
            self.car_x += 6
        if "up" in self.keys and self.speed < 15:
            self.speed += 1
        if "down" in self.keys and self.speed > 4:
            self.speed -= 1

        self.car_x = max(88, min(400, self.car_x))
        self.road_offset = (self.road_offset + self.speed) % 130

        for car in self.traffic:
            car[1] += self.speed
            if car[1] > HEIGHT:
                car[1] = -random.randint(120, 720)
                car[0] = random.choice([130, 245, 360])

        self.check_collisions()
        self.score += 1

    def intersects(self, a, b):
        ax, ay, aw, ah = a
        bx, by, bw, bh = b
        return ax < bx+bw and ax+aw > bx and ay < by+bh and ay+ah > by

    def check_collisions(self):
        player = [self.car_x+5, self.car_y+5, self.car_w-10, self.car_h-10]
        for car in self.traffic:
            if self.intersects(player, [car[0]+5, car[1]+5, car[2]-10, car[3]-10]):
                self.game_over = True

    def restart(self):
        self.car_x = 245
        self.speed = 7
        self.score = 0
        self.game_over = False
        self.traffic = [
            [130, -100, 52, 92, "#d22828"],
            [245, -380, 52, 92, "#f0a01e"],
            [360, -650, 52, 92, "#50c8c8"]
        ]

    def draw(self):
        self.canvas.delete("all")
        self.canvas.create_rectangle(0, 0, WIDTH, HEIGHT, fill="#145523", outline="")
        self.canvas.create_rectangle(70, 0, 470, HEIGHT, fill="#232323", outline="")
        for y in range(-60 + self.road_offset, HEIGHT, 80):
            self.canvas.create_rectangle(70, y, 470, y+30, fill="#464646", outline="")
        self.canvas.create_rectangle(70, 0, 78, HEIGHT, fill="white", outline="")
        self.canvas.create_rectangle(462, 0, 470, HEIGHT, fill="white", outline="")

        for y in range(-80 + self.road_offset, HEIGHT, 130):
            self.canvas.create_rectangle(266, y, 274, y+75, fill="yellow", outline="")

        for i in range(14):
            x = 20 if i % 2 == 0 else 485
            y = (i * 75 + self.road_offset) % HEIGHT
            self.canvas.create_oval(x, y, x+34, y+26, fill="#0f3c19", outline="")
            self.canvas.create_rectangle(x+14, y+20, x+20, y+42, fill="#5a3719", outline="")

        for car in self.traffic:
            self.draw_car(car[0], car[1], car[4], False)

        self.draw_car(self.car_x, self.car_y, "#2355e6", True)

        self.canvas.create_rectangle(12, 12, 528, 54, fill="#000000", outline="#444444")
        self.canvas.create_text(85, 36, text=f"Score: {self.score}", fill="white", font=("Arial", 17, "bold"))
        self.canvas.create_text(270, 36, text="Arrows to drive", fill="white", font=("Arial", 15, "bold"))
        self.canvas.create_text(455, 36, text=f"Speed: {self.speed}", fill="white", font=("Arial", 17, "bold"))

        if self.game_over:
            self.canvas.create_rectangle(0, 295, WIDTH, 405, fill="#000000")
            self.canvas.create_text(WIDTH//2, 350, text="CRASH!", fill="red", font=("Arial", 42, "bold"))
            self.canvas.create_text(WIDTH//2, 385, text="Press SPACE to restart", fill="white", font=("Arial", 18, "bold"))

    def loop(self):
        self.update()
        self.draw()
        self.root.after(16, self.loop)

EnhancedRacing()
