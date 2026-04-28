import turtle

screen = turtle.Screen()
screen.bgcolor("white")
screen.title("Fractal Tree Model")

tree = turtle.Turtle()
tree.speed(0)
tree.color("green")
tree.left(90)
tree.penup()
tree.goto(0, -250)
tree.pendown()

def draw_tree(branch_length, level):
    if level == 0:
        return

    tree.forward(branch_length)

    tree.right(30)
    draw_tree(branch_length * 0.7, level - 1)

    tree.left(60)
    draw_tree(branch_length * 0.7, level - 1)

    tree.right(30)

    tree.backward(branch_length)

draw_tree(100, 8)

screen.mainloop()