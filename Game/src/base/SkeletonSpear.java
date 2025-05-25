package base;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class SkeletonSpear implements Enemy {
    private BufferedImage[] idleFrames;
    private BufferedImage[] runFrames;
    private BufferedImage[] attackFrames;
    private BufferedImage[] hurtFrames;
    private BufferedImage[] deadFrames;

    private int frameIndex = 0;
    private int frameDelay = 8;
    private int frameCounter = 0;
    private int x, y;
    private final int frameHeight = 128;

    private boolean isHurt = false;
    private boolean isDead = false;
    private boolean visible = true;
    private boolean facingLeft = true;

    private int health = 100;
    private long lastDamageTime = 0;
    private final int hurtCooldown = 500;

    private final int speed = 2;
    private final int width = 64;
    private final int height = 128;

    private Knight knight;

    private int attackCount = 0;
    private boolean inAttackCooldown = false;
    private long attackCooldownStart = 0;
    private final int attackCooldownDuration = 2000;

    private enum State { IDLE, RUNNING, ATTACKING, HURT, DEAD }
    private State currentState = State.IDLE;

    public SkeletonSpear(int x, int y) {
        this.x = x;
        this.y = y;
        loadFrames();
    }

    private void loadFrames() {
        idleFrames = loadSpriteSheet("assets/skeleton_spear/Idle.png", 7);
        runFrames = loadSpriteSheet("assets/skeleton_spear/Run.png", 6);
        attackFrames = loadSpriteSheet("assets/skeleton_spear/Attack_1.png", 4);
        hurtFrames = loadSpriteSheet("assets/skeleton_spear/Hurt.png", 3);
        deadFrames = loadSpriteSheet("assets/skeleton_spear/Dead.png", 5);
    }

    private BufferedImage[] loadSpriteSheet(String path, int frameCount) {
        try {
            BufferedImage sheet = ImageIO.read(new File(path));
            int frameWidth = sheet.getWidth() / frameCount;
            BufferedImage[] frames = new BufferedImage[frameCount];
            for (int i = 0; i < frameCount; i++) {
                frames[i] = sheet.getSubimage(i * frameWidth, 0, frameWidth, frameHeight);
            }
            return frames;
        } catch (Exception e) {
            System.err.println("Failed loading sprite sheet: " + path);
            e.printStackTrace();
            return new BufferedImage[0];
        }
    }

    @Override
    public void update() {
        if (isDead) {
            currentState = State.DEAD;
            if (frameIndex >= deadFrames.length - 1) {
                visible = false;
                return;
            }
        } else if (isHurt) {
            currentState = State.HURT;
            if (System.currentTimeMillis() - lastDamageTime >= hurtCooldown) {
                isHurt = false;
            }
        } else if (inAttackCooldown) {
            if (System.currentTimeMillis() - attackCooldownStart >= attackCooldownDuration) {
                inAttackCooldown = false;
                attackCount = 0;
            }
            currentState = State.IDLE;
        } else {
            if (knight != null) {
                int distance = knight.getX() - x;

                if (Math.abs(distance) < 40) {
                    currentState = State.ATTACKING;

                    if (frameIndex == 2) {
                        if (getBounds().intersects(knight.getBounds())) {
                            knight.takeDamage(15);
                            attackCount++;
                            if (attackCount >= 3) {
                                inAttackCooldown = true;
                                attackCooldownStart = System.currentTimeMillis();
                            }
                        }
                    }

                    if (frameIndex >= attackFrames.length - 1) {
                        frameIndex = 0;
                        currentState = State.IDLE;
                    }

                } else {
                    if (distance < 0) {
                        x -= speed;
                        facingLeft = true;
                    } else {
                        x += speed;
                        facingLeft = false;
                    }
                    currentState = State.RUNNING;
                }
            } else {
                currentState = State.IDLE;
            }
        }

        frameCounter++;
        if (frameCounter >= frameDelay) {
            frameCounter = 0;
            frameIndex++;

            switch (currentState) {
                case DEAD -> frameIndex = Math.min(frameIndex, deadFrames.length - 1);
                case HURT -> frameIndex %= hurtFrames.length;
                case RUNNING -> frameIndex %= runFrames.length;
                case IDLE -> frameIndex %= idleFrames.length;
                case ATTACKING -> frameIndex %= attackFrames.length;
            }
        }
    }

    @Override
    public void draw(Graphics g) {
        if (!visible) return;

        BufferedImage currentFrame;

        if (isDead) {
            currentFrame = deadFrames[Math.min(frameIndex, deadFrames.length - 1)];
        } else if (isHurt) {
            currentFrame = hurtFrames[Math.min(frameIndex, hurtFrames.length - 1)];
        } else {
            switch (currentState) {
                case IDLE -> currentFrame = idleFrames[frameIndex % idleFrames.length];
                case RUNNING -> currentFrame = runFrames[frameIndex % runFrames.length];
                case ATTACKING -> currentFrame = attackFrames[frameIndex % attackFrames.length];
                default -> currentFrame = idleFrames[0];
            }
        }

        if (facingLeft) {
            g.drawImage(flipImage(currentFrame), x, y, null);
        } else {
            g.drawImage(currentFrame, x, y, null);
        }
    }

    @Override
    public void takeDamage(int damage) {
        if (System.currentTimeMillis() - lastDamageTime < hurtCooldown || isDead) return;
        health -= damage;
        lastDamageTime = System.currentTimeMillis();
        isHurt = true;
        frameIndex = 0;

        if (health <= 0) {
            isDead = true;
            frameIndex = 0;
            currentState = State.DEAD;
        }
    }

    @Override
    public boolean isDead() {
        return isDead;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public void setX(int x) {
        this.x = x;
    }

    @Override
    public void setSamurai(Knight knight) {
        this.knight = knight;
    }

    private BufferedImage flipImage(BufferedImage img) {
        BufferedImage flipped = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
        Graphics2D g2d = flipped.createGraphics();
        g2d.drawImage(img, img.getWidth(), 0, -img.getWidth(), img.getHeight(), null);
        g2d.dispose();
        return flipped;
    }
}
