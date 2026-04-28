import tkinter as tk
import math

WIDTH, HEIGHT = 900, 650

class Unit:
    def __init__(self, x, y, friendly=True):
        self.x = x
        self.y = y
        self.target_x = x
        self.target_y = y
        self.health = 100
        self.friendly = friendly
        self.speed = 1.5

    def move(self):
        dx = self.target_x - self.x
        dy = self.target_y - self.y
        dist = math.hypot(dx, dy)

        if dist > self.speed:
            self.x += self.speed * dx / dist
            self.y += self.speed * dy / dist

class Building:
    def __init__(self, x, y, kind):
        self.x = x
        self.y = y
        self.kind = kind

class RTS:
    def __init__(self):
        self.root = tk.Tk()
        self.root.title("Python Mini Age of Empires Style RTS")
        self.canvas = tk.Canvas(self.root, width=WIDTH, height=HEIGHT, bg="#469646")
        self.canvas.pack()

        self.units = [Unit(180, 160), Unit(220, 160)]
        self.enemies = [Unit(760, 500, False), Unit(720, 520, False)]
        self.buildings = [Building(100, 100, "Town Center")]
        self.selected = None

        self.wood = 200
        self.food = 100
        self.spawn_timer = 0
        self.game_over = False

        self.canvas.bind("<Button-1>", self.left_click)
        self.canvas.bind("<Button-3>", self.right_click)
        self.root.bind("<KeyPress>", self.key_press)

        self.loop()
        self.root.mainloop()

    def left_click(self, e):
        self.selected = None
        for unit in self.units:
            if math.hypot(e.x - unit.x, e.y - unit.y) < 20:
                self.selected = unit
                break

    def right_click(self, e):
        if self.selected:
            self.selected.target_x = e.x
            self.selected.target_y = e.y

    def key_press(self, e):
        key = e.keysym.lower()

        if key == "v" and self.food >= 50:
            self.units.append(Unit(140, 160))
            self.food -= 50

        if key == "b" and self.wood >= 75 and self.selected:
            self.buildings.append(Building(self.selected.x, self.selected.y, "Hut"))
            self.wood -= 75

    def update(self):
        if self.game_over:
            return

        for unit in self.units + self.enemies:
            unit.move()

        for enemy in self.enemies:
            nearest = None
            best = 999999

            for unit in self.units:
                d = math.hypot(enemy.x - unit.x, enemy.y - unit.y)
                if d < best:
                    best = d
                    nearest = unit

            if nearest and best < 250:
                enemy.target_x = nearest.x
                enemy.target_y = nearest.y

        for unit in self.units[:]:
            for enemy in self.enemies[:]:
                if math.hypot(unit.x - enemy.x, unit.y - enemy.y) < 28:
                    unit.health -= 1
                    enemy.health -= 1

                    if enemy.health <= 0:
                        self.enemies.remove(enemy)
                        self.food += 25

                    if unit.health <= 0:
                        self.units.remove(unit)
                        break

        self.spawn_timer += 1
        if self.spawn_timer > 500:
            self.enemies.append(Unit(800, 500, False))
            self.spawn_timer = 0

        if not self.units:
            self.game_over = True

    def draw(self):
        self.canvas.delete("all")

        self.canvas.create_rectangle(0, 0, WIDTH, 45, fill="black")
        self.canvas.create_text(140, 24, text=f"Wood: {self.wood}   Food: {self.food}", fill="white", font=("Arial", 16, "bold"))
        self.canvas.create_text(560, 24, text="Left click select | Right click move/attack | V train villager | B build hut",
                                fill="white", font=("Arial", 14, "bold"))

        for i in range(8):
            self.canvas.create_oval(520+i*30, 60+(i%2)*30, 545+i*30, 85+(i%2)*30, fill="#503c1e")

        for building in self.buildings:
            color = "orange" if building.kind == "Town Center" else "gray"
            self.canvas.create_rectangle(building.x-35, building.y-35, building.x+35, building.y+35, fill=color, outline="black")
            self.canvas.create_text(building.x, building.y-45, text=building.kind, fill="black", font=("Arial", 10, "bold"))

        for unit in self.units + self.enemies:
            color = "blue" if unit.friendly else "red"
            self.canvas.create_oval(unit.x-14, unit.y-14, unit.x+14, unit.y+14, fill=color)
            self.canvas.create_text(unit.x, unit.y-22, text=str(unit.health), fill="white")

            if unit == self.selected:
                self.canvas.create_oval(unit.x-20, unit.y-20, unit.x+20, unit.y+20, outline="yellow", width=3)

        if self.game_over:
            self.canvas.create_text(WIDTH//2, HEIGHT//2, text="GAME OVER", fill="white", font=("Arial", 42, "bold"))

    def loop(self):
        self.update()
        self.draw()
        self.root.after(16, self.loop)

RTS()
