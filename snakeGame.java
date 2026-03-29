package simple2dgameproject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;
import java.util.Random;

public class simpleJavaGame2 extends JPanel implements ActionListener, KeyListener {

 
    LinkedList<Point> snake = new LinkedList<>();
    int size = 20;

    int directionX = size; 
    int directionY = 0;

    int targetX, targetY;
    int score = 0;
    int targetScore = 10;
    int timeLeft = 30;

    boolean grow = false;
    JButton restartBtn;
    

    javax.swing.Timer gameTimer;
    Random rand = new Random();

    boolean gameOver = false;
    boolean win = false;

    public simpleJavaGame2() {
        setPreferredSize(new Dimension(400, 400));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        // initial snake
        snake.add(new Point(100, 100));

        spawnTarget();

        // GAME LOOP (auto movement)
        gameTimer = new javax.swing.Timer(200, this);//you can change snake speed
        gameTimer.start();

        // TIMER (countdown)
        new javax.swing.Timer(1000, e -> {
            if (!gameOver) {
                timeLeft--;
                if (timeLeft <= 0) {
                    gameOver = true;
                }
            }
        }).start();
        
        setLayout(null); // IMPORTANT for positioning button

		restartBtn = new JButton("Restart");
		restartBtn.setBounds(150, 220, 100, 30);
		restartBtn.setVisible(false);

		restartBtn.addActionListener(e -> resetGame());

		add(restartBtn);
	}

    void spawnTarget() {
        targetX = rand.nextInt(400 / size) * size;
        targetY = rand.nextInt(400 / size) * size;
    }

    //  GAME LOOP
    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            moveSnake();
            checkCollision();
            checkWallCollision();
            repaint();
        }
    }

    void moveSnake() {
        Point head = snake.getFirst();
        Point newHead = new Point(head.x + directionX, head.y + directionY);

        snake.addFirst(newHead);

        if (!grow) {
            snake.removeLast();
        } else {
            grow = false;
        }
    }

    void checkCollision() {
        Point head = snake.getFirst();

        Rectangle snakeHead = new Rectangle(head.x, head.y, size, size);
        Rectangle target = new Rectangle(targetX, targetY, size, size);

        if (snakeHead.intersects(target)) {
            score++;
            grow = true;
            spawnTarget();

            if (score >= targetScore) {
                win = true;
                gameOver = true;
            }
        }
    }

    void checkWallCollision() {
        Point head = snake.getFirst();

        if (head.x < 0 || head.y < 0 || head.x >= 400 || head.y >= 400) {
            gameOver = true;
        }
    }

    void resetGame() {
    snake.clear();
    snake.add(new Point(100, 100));

    directionX = size;
    directionY = 0;

    score = 0;
    timeLeft = 30;
    gameOver = false;
    win = false;
    grow = false;

    spawnTarget();

    restartBtn.setVisible(false);

    gameTimer.start();
}
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (!gameOver) {

            
            g.setColor(Color.BLUE);
            for (Point p : snake) {
                g.fillOval(p.x, p.y, size, size);
            }

            
            g.setColor(Color.RED);
            g.fillOval(targetX, targetY, size, size);

            
            g.setColor(Color.WHITE);
            g.drawString("Score: " + score, 10, 20);
            g.drawString("Time: " + timeLeft, 300, 20);

        } else {
            g.setColor(Color.WHITE);
            if (win) {
                g.drawString("YOU WIN!", 150, 200);
            } else {
                g.drawString("GAME OVER", 140, 200);
            }
            restartBtn.setVisible(true);
        }
    }

    
    @Override
    public void keyPressed(KeyEvent e) {

        if (e.getKeyCode() == KeyEvent.VK_LEFT && directionX == 0) {
            directionX = -size;
            directionY = 0;
        }
        if (e.getKeyCode() == KeyEvent.VK_RIGHT && directionX == 0) {
            directionX = size;
            directionY = 0;
        }
        if (e.getKeyCode() == KeyEvent.VK_UP && directionY == 0) {
            directionX = 0;
            directionY = -size;
        }
        if (e.getKeyCode() == KeyEvent.VK_DOWN && directionY == 0) {
            directionX = 0;
            directionY = size;
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Snake Target Game");
        simpleJavaGame2 game = new simpleJavaGame2();

        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}