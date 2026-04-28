import tkinter as tk
import math

WIDTH, HEIGHT = 950, 680

class Unit:
    def __init__(self, x, y, friendly=True):
        self.x, self.y = x, y
        self.target_x, self.target_y = x, y
        self.health = 100
        self.friendly = friendly
        self.speed = 1.5

    def move(self):
        dx, dy = self.target_x - self.x, self.target_y - self.y
        dist = math.hypot(dx, dy)
        if dist > self.speed:
            self.x += self.speed * dx / dist
            self.y += self.speed * dy / dist

class Building:
    def __init__(self, x, y, kind):
        self.x, self.y, self.kind = x, y, kind

class EnhancedRTS:
    def __init__(self):
        self.root = tk.Tk()
        self.root.title("Enhanced Python Age of Empires Style RTS")
        self.canvas = tk.Canvas(self.root, width=WIDTH, height=HEIGHT, bg="#4b9b4b")
        self.canvas.pack()

        self.units = [Unit(190,170), Unit(230,170)]
        self.enemies = [Unit(790,510,False), Unit(750,535,False)]
        self.buildings = [Building(110,115,"Town Center")]
        self.selected = None
        self.wood = 250
        self.food = 150
        self.spawn_timer = 0
        self.game_over = False

        self.canvas.bind("<Button-1>", self.left)
        self.canvas.bind("<Button-3>", self.right)
        self.root.bind("<KeyPress>", self.key)

        self.loop()
        self.root.mainloop()

    def left(self, e):
        self.selected = None
        for u in self.units:
            if math.hypot(e.x-u.x, e.y-u.y) < 22:
                self.selected = u
                break

    def right(self, e):
        if self.selected:
            self.selected.target_x, self.selected.target_y = e.x, e.y

    def key(self, e):
        key = e.keysym.lower()
        if key == "v" and self.food >= 50:
            self.units.append(Unit(155,170))
            self.food -= 50
        if key == "b" and self.wood >= 75 and self.selected:
            self.buildings.append(Building(self.selected.x, self.selected.y, "Hut"))
            self.wood -= 75

    def update(self):
        if self.game_over:
            return

        for u in self.units + self.enemies:
            u.move()

        for enemy in self.enemies:
            nearest, best = None, 99999
            for u in self.units:
                d = math.hypot(enemy.x-u.x, enemy.y-u.y)
                if d < best:
                    best, nearest = d, u
            if nearest and best < 275:
                enemy.target_x, enemy.target_y = nearest.x, nearest.y

        for u in self.units[:]:
            for enemy in self.enemies[:]:
                if math.hypot(u.x-enemy.x, u.y-enemy.y) < 28:
                    u.health -= 1
                    enemy.health -= 1
                    if enemy.health <= 0:
                        self.enemies.remove(enemy)
                        self.food += 25
                    if u.health <= 0:
                        self.units.remove(u)
                        break

        self.spawn_timer += 1
        if self.spawn_timer > 520:
            self.enemies.append(Unit(820,520,False))
            self.spawn_timer = 0

        if not self.units:
            self.game_over = True

    def draw_building(self, b):
        x, y = b.x, b.y
        if b.kind == "Town Center":
            self.canvas.create_rectangle(x-50, y-30, x+50, y+40, fill="#784b23", outline="black")
            self.canvas.create_polygon(x-60, y-30, x, y-75, x+60, y-30, fill="#962819", outline="black")
        else:
            self.canvas.create_rectangle(x-30, y-25, x+30, y+25, fill="#735037", outline="black")
            self.canvas.create_polygon(x-35, y-25, x, y-55, x+35, y-25, fill="#5a2d19", outline="black")
        self.canvas.create_text(x, y+55, text=b.kind, fill="black", font=("Arial", 10, "bold"))

    def draw_unit(self, u):
        color = "#2850d2" if u.friendly else "#b42823"
        self.canvas.create_oval(u.x-15, u.y+10, u.x+15, u.y+20, fill="#000000", outline="")
        self.canvas.create_oval(u.x-12, u.y-18, u.x+12, u.y+6, fill=color, outline="")
        self.canvas.create_rectangle(u.x-10, u.y, u.x+10, u.y+28, fill=color, outline="")
        self.canvas.create_oval(u.x-8, u.y-26, u.x+8, u.y-10, fill="#e6be8c", outline="")
        self.canvas.create_text(u.x, u.y-34, text=str(u.health), fill="white", font=("Arial", 9, "bold"))
        if u == self.selected:
            self.canvas.create_oval(u.x-24, u.y-30, u.x+24, u.y+32, outline="yellow", width=3)

    def draw(self):
        self.canvas.delete("all")
        self.canvas.create_rectangle(0, 0, WIDTH, HEIGHT, fill="#4b9b4b", outline="")

        for i in range(10):
            x = 540 + i * 31
            y = 70 + (i % 2) * 28
            self.canvas.create_oval(x, y, x+26, y+18, fill="#503219", outline="")
            self.canvas.create_oval(x-5, y-18, x+31, y+12, fill="#236423", outline="")

        self.canvas.create_rectangle(10,10,940,52, fill="#000000", outline="#444")
        self.canvas.create_text(145,32, text=f"Wood: {self.wood}   Food: {self.food}", fill="white", font=("Arial", 16, "bold"))
        self.canvas.create_text(595,32, text="Left click select | Right click move/attack | V train villager | B build hut",
                                fill="white", font=("Arial", 13, "bold"))

        self.canvas.create_rectangle(620,500,880,555, fill="#3c5a96", outline="")
        self.canvas.create_rectangle(640,510,850,530, fill="#8cbede", outline="")

        for b in self.buildings:
            self.draw_building(b)
        for u in self.units + self.enemies:
            self.draw_unit(u)

        if self.game_over:
            self.canvas.create_rectangle(0,280,WIDTH,380, fill="#000000")
            self.canvas.create_text(WIDTH//2,345, text="YOUR VILLAGE HAS FALLEN", fill="red", font=("Arial", 38, "bold"))

    def loop(self):
        self.update()
        self.draw()
        self.root.after(16, self.loop)

EnhancedRTS()
