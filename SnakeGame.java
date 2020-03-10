import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.LinkedList;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.Timer;

public class SnakeGame extends JFrame {
	// A snake is just a list of coordinates (java.util.LinkedList, not our List)
	private LinkedList<Coordinate> snake      = new LinkedList<Coordinate>();
	// The snake grows when it eats food
	private LinkedList<Coordinate> foodList   = new LinkedList<Coordinate>();
	// Game ends when snake eats the poison
	private LinkedList<Coordinate> poisonList = new LinkedList<Coordinate>();

	// The game is on or over
	private static enum Game { ON, OVER};
	private Game status = Game.ON;

	// Repeatedly moves the snake
	private Timer timer;
	private int delay = 150;
	private int foodCounter = 0;
	
	// The snake can move in one of 4 directions
	public static enum Direction { UP, DOWN, LEFT, RIGHT };
	// The snake's current direction (heading). Default: moving right
	private Direction heading = Direction.RIGHT;

	// The snake can't switch to the opposite direction
	public boolean oppositeDirection(Direction newHeading) {
		return (heading == Direction.UP && newHeading == Direction.DOWN) ||
				(heading == Direction.DOWN && newHeading == Direction.UP) ||
				(heading == Direction.LEFT && newHeading == Direction.RIGHT) ||
				(heading == Direction.RIGHT && newHeading == Direction.LEFT);
	}

	// Update the heading based on the new heading
	public void changeHeading(Direction newHeading) {
		if (!oppositeDirection(newHeading)) {
			heading = newHeading;
		}
	}

	// Handle keyboard input (arrows change the snake's heading)
	private class KeyControl implements KeyListener {
		@Override
		public void keyTyped(KeyEvent e) {}

		@Override
		public void keyPressed(KeyEvent e) {
			Direction newHeading = heading;

			switch(e.getKeyCode()) {
			case KeyEvent.VK_LEFT: case KeyEvent.VK_KP_LEFT:
				newHeading = Direction.LEFT; break;
			case KeyEvent.VK_RIGHT: case KeyEvent.VK_KP_RIGHT:
				newHeading = Direction.RIGHT; break;
			case KeyEvent.VK_UP: case KeyEvent.VK_KP_UP:
				newHeading = Direction.UP; break;
			case KeyEvent.VK_DOWN: case KeyEvent.VK_KP_DOWN:
				newHeading = Direction.DOWN; break;
			}
			changeHeading(newHeading);
		}

		@Override
		public void keyReleased(KeyEvent e) {
		}
	}

	// An (x,y) coordinate in a 64 by 48 grid
	public static class Coordinate {
		public final int x;
		public final int y;
		// By default, construct a random coordinate not too far from the wall
		Coordinate() {
			this.x = new Random().nextInt(60) + 2;
			this.y = new Random().nextInt(40) + 2;
		}
		Coordinate(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}
	@Override
	// This view renders the snake and food
	// Each snake coordinate is a 10x10 pixel square
	public void paint(Graphics g) {
		g.clearRect(0, 0, 640, 480);
		Color green = new Color(0,128,0);
		g.setColor(green);
		
		for (Coordinate c : snake) {
			g.fillRect(c.x*10, c.y*10, 10, 10);
		}
		g.setColor(Color.BLUE);
		for (Coordinate food : foodList)
			g.fillOval(food.x*10, food.y*10, 10, 10);
		
		g.setColor(Color.RED);
		for (Coordinate poison : poisonList)
			g.fillOval(poison.x*10, poison.y*10, 10, 10);
	}

	// The snake's heading determines its new head coordinate
	private Coordinate newHead() {
		Coordinate head, newHead;
		head = snake.getFirst();

		switch (heading) {
		case DOWN: newHead  = new Coordinate(head.x, head.y + 1); break;
		case LEFT: newHead  = new Coordinate(head.x - 1, head.y); break;
		case RIGHT: newHead = new Coordinate(head.x + 1, head.y); break;
		case UP: newHead    = new Coordinate(head.x, head.y - 1); break;
		// The default case is never reached because we have only 4 events.
		default: newHead    = new Coordinate(); break;
		}
		return newHead;
	}

	// When the snake moves, it can hit the wall, hit the food, poison (not implemented) or itself (not implemented)
	public void move() {
		Coordinate newHead = newHead();

		if (hitTheWall(newHead)) {
			status = Game.OVER;
			return; // will return back to where this method is called
		}
		
		if (hitThePoison(newHead)) {
			status = Game.OVER;
			return;
		}

		if (hitSelf(newHead)) {
			status = Game.OVER;
			return;
		}
		
		snake.addFirst(newHead);

		if (hitTheFood(newHead)) {
			foodList.add(new Coordinate());
			poisonList.add(new Coordinate());
			delay = delay - 5;
			timer.setDelay(delay);
			foodCounter++;
		} else {
			snake.removeLast();
		}
		
		if (foodCounter >= 20)
		{
			status = Game.OVER;
			playAgain("You won!");
		}
	}

	private boolean hitTheFood(Coordinate newHead) {
		for (Coordinate food : foodList) {
			if (newHead.x == food.x && newHead.y == food.y) {
				foodList.remove(food);
				return true;
			}
		}
		
		return false;
	}
	
	public boolean hitThePoison(Coordinate newHead) {
		for (Coordinate poison : poisonList) {
			if (newHead.x == poison.x && newHead.y == poison.y)
				return true;
		}
		return false;
	}

	public boolean hitTheWall(Coordinate head) {
		return (head.x == 64 || head.y == 48 || head.x == 0 || head.y == 0);
	}
	
	public boolean hitSelf(Coordinate newHead) {
		for (Coordinate h : snake)
			if ( h.x == newHead.x && h.y == newHead.y)
				return true;
		return false;
	}
	


	// The timer moves the snake using this class. 
	private class SnakeMover implements ActionListener {
		@Override
		// Listening Action (in this case Timer - every certain millisecond) and execute this method
		public void actionPerformed(ActionEvent e) {
			move();
			repaint();	// from AWT library. It will call paint() automatically
			if (status == Game.OVER) {
				playAgain("The snake's dead");
			}
		}
	}

	// Ask the player what to do when the game is over
	private void playAgain(String message) {
		String[] options = new String[] {"Play again","Quit"};
		int choice = JOptionPane.showOptionDialog(null, message, "Game over", JOptionPane.YES_NO_OPTION, 
				JOptionPane.QUESTION_MESSAGE,null,options,options[0]);
		delay = 150; //reset speed

		if (choice == 0) {
			initialize();
		} else {
			System.exit(0);
		}
	}

	// Initialize game (snake, food, etc)
	private void initialize() {
		status = Game.ON;
		foodCounter = 0;
		timer.setDelay(150);
		// Make a small snake with 1 node (a 10x10 pixel coordinate)
		snake.clear(); // remove all of the elements of the LinkedList
		snake.add(new Coordinate()); // append the new element to the end of the LinkedList
		foodList.clear();

		for (int i = 0; i < 3; i++) {
			foodList.add(new Coordinate());
		}

		poisonList.clear();
		
		for (int i = 0; i < 3; i++) {
			poisonList.add(new Coordinate());
		}
	}
	public SnakeGame() {

		setSize(640, 480);	// Window size - pixel
		setTitle("Snake Game");
		setVisible(true);

		// Update the snake's direction using keyboard arrows
		// Event Handler: addKeyListener is from AWT library. This is how to "register" event
		addKeyListener(new KeyControl());

		// Make the snake move every 150 milliseconds
		timer = new Timer(delay, new SnakeMover());
		timer.start();

		// Initialize game (snake, food)
		initialize();

	}

	public static void main(String[] args) {
		new SnakeGame();
	}
}
