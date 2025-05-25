package base;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;

public class SkeletonArcher {
    private int x, y;
    private int health = 100;
    private boolean facingLeft;
    private boolean isDead = false;
    private boolean isHurt = false;

    private int frameIndex = 0, frameTimer = 0, frameDelay = 6;
    private BufferedImage[] idleFrames, hurtFrames, shootFrames;
    private BufferedImage deathFrame, arrowImage;

    private ArrayList<Arrow> arrows = new ArrayList<>();
    private long lastShotTime = 0;
    private final long shootCooldown = 2000; // milliseconds

    private Knight knight; // Reference to the player
    private boolean isShooting = false;

    public SkeletonArcher(int x, int y, Knight knight) {
        this.x = x;
        this.y = y;
        this.knight = knight;
        loadSprites();
    }

    private void loadSprites() {
        idleFrames = loadFrames("assets/skeletonarcher/Idle.png", 7);
        hurtFrames = loadFrames("assets/skeletonarcher/Hurt.png", 2);
        shootFrames = loadFrames("assets/skeletonarcher/Shot_1.png", 15);
        try {
            arrowImage = ImageIO.read(new File("assets/skeletonarcher/Arrow.png"));
            deathFrame = ImageIO.read(new File("assets/skeletonarcher/Dead.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private BufferedImage[] loadFrames(String path, int count) {
        try {
            BufferedImage sheet = ImageIO.read(new File(path));
            BufferedImage[] frames = new BufferedImage[count];
            int frameWidth = sheet.getWidth() / count;
            for (int i = 0; i < count; i++) {
                frames[i] = sheet.getSubimage(i * frameWidth, 0, frameWidth, sheet.getHeight());
            }
            return frames;
        } catch (IOException e) {
            e.printStackTrace();
            return new BufferedImage[0];
        }
    }

    public void update() {
        if (isDead) return;

        facingLeft = knight.getX() < x;

        int distance = Math.abs(knight.getX() - x);
        boolean knightInRange = distance <= 400;

        if (knightInRange && System.currentTimeMillis() - lastShotTime >= shootCooldown) {
            shootArrow();
            lastShotTime = System.currentTimeMillis();
            isShooting = true;
            frameIndex = 0;
        }

        frameTimer++;
        if (frameTimer >= frameDelay) {
            frameTimer = 0;
            frameIndex++;
            if (isShooting) {
                if (frameIndex >= shootFrames.length) {
                    frameIndex = 0;
                    isShooting = false;
                }
            } else if (isHurt) {
                if (frameIndex >= hurtFrames.length) {
                    isHurt = false;
                    frameIndex = 0;
                }
            } else {
                frameIndex %= idleFrames.length;
            }
        }

        // Update arrows
        for (Arrow arrow : arrows) {
            arrow.update();
        }
        arrows.removeIf(a -> !a.isActive());
    }

    public void draw(Graphics g, int cameraX) {
        BufferedImage frame;
        if (isDead) {
            frame = deathFrame;
        } else if (isHurt) {
            frame = hurtFrames[Math.min(frameIndex, hurtFrames.length - 1)];
        } else if (isShooting) {
            frame = shootFrames[Math.min(frameIndex, shootFrames.length - 1)];
        } else {
            frame = idleFrames[Math.min(frameIndex, idleFrames.length - 1)];
        }

        int drawX = x - cameraX;
        if (facingLeft) {
            g.drawImage(flipImage(frame), drawX, y, null);
        } else {
            g.drawImage(frame, drawX, y, null);
        }

        for (Arrow arrow : arrows) {
            arrow.draw(g, cameraX);
        }
    }

    private void shootArrow() {
        int arrowX = facingLeft ? x : x + 64;
        int arrowY = y + 50;
        arrows.add(new Arrow(arrowX, arrowY, facingLeft, arrowImage));
    }

    public void checkHit(Rectangle swordBox) {
        if (!isDead && swordBox.intersects(getBounds())) {
            health -= 50;
            isHurt = true;
            frameIndex = 0;
            if (health <= 0) {
                isDead = true;
                System.out.println("Skeleton Archer defeated!");
            }
        }
    }

    public ArrayList<Arrow> getArrows() {
        return arrows;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, 64, 128);
    }

    public boolean isDead() {
        return isDead;
    }

    private BufferedImage flipImage(BufferedImage img) {
        BufferedImage flipped = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
        Graphics2D g2d = flipped.createGraphics();
        g2d.drawImage(img, img.getWidth(), 0, -img.getWidth(), img.getHeight(), null);
        g2d.dispose();
        return flipped;
    }

    public int getX() {
        return x;
    }

    public void setX(int newX) {
        x = newX;
    }
}
