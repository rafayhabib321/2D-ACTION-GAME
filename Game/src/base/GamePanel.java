package base;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class GamePanel extends JPanel implements ActionListener {
    private Timer timer;
    private Knight knight;
    private ArrayList<SkeletonWarrior> enemies;
    private ArrayList<SkeletonArcher> archers = new ArrayList<>();

    private long lastSpawnTime = System.currentTimeMillis();
    private final int spawnInterval = 5000;

    private BufferedImage background;
    private int cameraX = 0;

    private boolean enemySpawnedAfterScroll = false;

    public GamePanel() {
        setPreferredSize(new Dimension(800, 600));
        setFocusable(true);
        requestFocusInWindow();

        knight = new Knight();
        archers.add(new SkeletonArcher(900, 400, knight));
        enemies = new ArrayList<>();

        try {
            background = ImageIO.read(new File("assets/background/Battleground4.png"));
        } catch (Exception e) {
            System.err.println("Failed to load background");
            e.printStackTrace();
        }

        spawnEnemy();

        addKeyListener(new InputHandler(knight));
        timer = new Timer(16, this); // ~60 FPS
    }

    public void startGameLoop() {
        timer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        knight.update();

        for (SkeletonArcher archer : archers) {
            archer.update();
            if (knight.isInAttackHitFrame()) {
                archer.checkHit(knight.getAttackBox());
            }
            for (Arrow arrow : archer.getArrows()) {
                if (arrow.getBounds().intersects(knight.getBounds())) {
                    knight.takeDamage(10);
                }
            }
        }

        if (knight.getX() > 400) {
            int dx = knight.getX() - 400;
            cameraX += dx;
            knight.setX(400); // Fix Knight at screen center

            for (SkeletonWarrior enemy : enemies) {
                enemy.setX(enemy.getX() - dx);
            }
            for (SkeletonArcher archer : archers) {
                archer.setX(archer.getX() - dx);
            }
        } else if (cameraX > 0 && knight.getX() < 400) {
            int dx = 400 - knight.getX();
            cameraX -= dx;
            if (cameraX < 0) {
                dx += cameraX;
                cameraX = 0;
            }
            knight.setX(400);
            for (SkeletonWarrior enemy : enemies) {
                enemy.setX(enemy.getX() + dx);
            }
            for (SkeletonArcher archer : archers) {
                archer.setX(archer.getX() + dx);
            }
        }

        if (cameraX > 800 && !enemySpawnedAfterScroll) {
            spawnEnemy();
            enemySpawnedAfterScroll = true;
        }

        Iterator<SkeletonWarrior> iterator = enemies.iterator();
        while (iterator.hasNext()) {
            SkeletonWarrior enemy = iterator.next();
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

        if (background != null) {
            int bgWidth = background.getWidth();
            int panelWidth = getWidth();

            int startX = -cameraX % bgWidth;
            if (startX > 0) startX -= bgWidth;

            for (int x = startX; x < panelWidth; x += bgWidth) {
                g.drawImage(background, x, 0, null);
            }
        }

        knight.draw(g);

        for (SkeletonArcher archer : archers) {
            archer.draw(g, cameraX);
        }

        for (SkeletonWarrior enemy : enemies) {
            enemy.draw(g);
        }
    }

    private void spawnEnemy() {
        int spawnX = 900;
        int spawnY = 400;
        SkeletonWarrior enemy = new SkeletonWarrior(spawnX + cameraX, spawnY);
        enemy.setSamurai(knight);
        enemies.add(enemy);
    }

    private boolean samuraiAttackHits(Knight knight, SkeletonWarrior skeleton) {
        Rectangle attackBox = knight.getAttackBox();
        Rectangle skeletonBox = new Rectangle(skeleton.getX(), skeleton.getY(), 64, 128);
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
