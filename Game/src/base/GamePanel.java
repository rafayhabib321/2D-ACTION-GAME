package base;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class GamePanel extends JPanel implements ActionListener {
    private Timer timer;
    private Knight knight;
    private ArrayList<Enemy> enemies;
    private long lastSpawnTime = System.currentTimeMillis();
    private final int spawnInterval = 5000;

    private BufferedImage background;
    private BufferedImage youDiedImage;
    private int cameraX = 0;

    private boolean enemySpawnedAfterScroll = false;
    
    public GamePanel() {
        setPreferredSize(new Dimension(800, 600));
        setFocusable(true);
        requestFocusInWindow();

        knight = new Knight();
        enemies = new ArrayList<>();

        try {
            background = ImageIO.read(new File("assets/background/Battleground4.png"));
        } catch (Exception e) {
            System.err.println("Failed to load background");
            e.printStackTrace();
        }
        try {
            youDiedImage = ImageIO.read(new File("assets/you_died.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        spawnEnemy(); // Initial enemy spawn

        addKeyListener(new InputHandler(knight));
        timer = new Timer(16, this); // ~60 FPS
    }

    public void startGameLoop() {
        timer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        knight.update();

        // Camera tracking logic
        if (knight.getX() > 400) {
            int dx = knight.getX() - 400;
            cameraX += dx;
            knight.setX(400);

            for (Enemy enemy : enemies) {
                enemy.setX(enemy.getX() - dx);
            }
        } else if (cameraX > 0 && knight.getX() < 400) {
            int dx = 400 - knight.getX();
            cameraX -= dx;
            if (cameraX < 0) {
                dx += cameraX;
                cameraX = 0;
            }
            knight.setX(400);

            for (Enemy enemy : enemies) {
                enemy.setX(enemy.getX() + dx);
            }
        }

        // Periodic enemy spawn
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSpawnTime >= spawnInterval) {
            spawnEnemy();
            lastSpawnTime = currentTime;
        }

        // Special spawn when scrolled far
        if (cameraX > 800 && !enemySpawnedAfterScroll) {
            spawnEnemy();
            enemySpawnedAfterScroll = true;
        }

        // Update enemies
        Iterator<Enemy> iterator = enemies.iterator();
        while (iterator.hasNext()) {
            Enemy enemy = iterator.next();
            enemy.update();

            if (knight.isInAttackHitFrame() && samuraiAttackHits(knight, enemy)) {
                enemy.takeDamage(20);
            }

            if (enemy.isDead() && !enemy.isVisible()) {
                iterator.remove();
            }
        }

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Draw scrolling background
        if (background != null) {
            int bgWidth = background.getWidth();
            int panelWidth = getWidth();

            int startX = -cameraX % bgWidth;
            if (startX > 0) startX -= bgWidth;

            for (int x = startX; x < panelWidth; x += bgWidth) {
                g2d.drawImage(background, x, 0, null);
            }
        }

        // Show "YOU DIED" screen if knight is dead
        if (knight.isDead()) {
            g2d.drawImage(youDiedImage, 0, 0, 800, 600, null); // Fullscreen image
            return;
        }

        // Draw knight and enemies
        knight.draw(g2d);
        for (Enemy enemy : enemies) {
            enemy.draw(g2d);
        }
    }


    private void spawnEnemy() {
        int spawnX = 900;
        int spawnY = 400;

        if (Math.random() < 0.5) {
            SkeletonWarrior warrior = new SkeletonWarrior(spawnX + cameraX, spawnY);
            warrior.setSamurai(knight);
            enemies.add(warrior);
            System.out.println("Spawned SkeletonWarrior");
        } else {
            SkeletonSpear spear = new SkeletonSpear(spawnX + cameraX, spawnY);
            spear.setSamurai(knight);
            enemies.add(spear);
            System.out.println("Spawned SkeletonSpear");
        }
    }

    private boolean samuraiAttackHits(Knight knight, Enemy enemy) {
        Rectangle attackBox = knight.getAttackBox();
        Rectangle skeletonBox = new Rectangle(enemy.getX(), enemy.getY(), 64, 128);
        return attackBox.intersects(skeletonBox);
    }

    private static class InputHandler extends KeyAdapter {
        private final Knight knight;

        public InputHandler(Knight knight) {
            this.knight = knight;
        }

        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_A -> knight.moveLeft(true);
                case KeyEvent.VK_D -> knight.moveRight(true);
                case KeyEvent.VK_W -> knight.jump();
                case KeyEvent.VK_Z -> knight.attack();
                case KeyEvent.VK_X -> knight.setDefending(true);
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_A -> knight.moveLeft(false);
                case KeyEvent.VK_D -> knight.moveRight(false);
                case KeyEvent.VK_X -> knight.setDefending(false);
            }
        }
    }
}
