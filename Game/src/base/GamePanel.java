package base;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GamePanel extends JPanel implements ActionListener {
    private Timer timer;
    private Samurai samurai;

    public GamePanel() {
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.BLACK);
        setFocusable(true);
        requestFocusInWindow();

        samurai = new Samurai();
        addKeyListener(new InputHandler(samurai));

        timer = new Timer(16, this); // ~60 FPS
    }

    // Start the game loop
    public void startGameLoop() {
        timer.start();
    }

    // Game update loop
    @Override
    public void actionPerformed(ActionEvent e) {
        samurai.update();
        repaint();
    }

    // Drawing
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        samurai.draw(g);
    }

    // Key input handler
    private static class InputHandler extends KeyAdapter {
        private final Samurai samurai;

        public InputHandler(Samurai samurai) {
            this.samurai = samurai;
        }

        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_A:
                    samurai.moveLeft(true);
                    break;
                case KeyEvent.VK_D:
                    samurai.moveRight(true);
                    break;
                case KeyEvent.VK_W:
                    samurai.jump();
                    break;
                case KeyEvent.VK_Z:
                    samurai.attack();
                    break;
                case KeyEvent.VK_X:
                    samurai.setDefending(true);
                    break;
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_A:
                    samurai.moveLeft(false);
                    break;
                case KeyEvent.VK_D:
                    samurai.moveRight(false);
                    break;
                case KeyEvent.VK_X:
                    samurai.setDefending(false);
                    break;
            }
        }
    }
}
