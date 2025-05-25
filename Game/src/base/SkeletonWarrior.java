package base;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class SkeletonWarrior {
    private BufferedImage[] idleFrames;
    private BufferedImage[] walkFrames;
    private BufferedImage[] attackFrames;
    private BufferedImage defendFrame;
    private BufferedImage[] hurtFrames;
    private BufferedImage[] deathFrames;

    private int frameIndex;
    private int frameDelay = 8;
    private int frameCounter;
    private int x, y;
    private int frameWidth = 128;
    private int frameHeight = 128;
    private boolean stunned = false;
    private long stunStartTime = 0;
    private int stunDuration = 1000;
    private int health = 100;
    private boolean isHurt = false;
    private boolean isDead = false;
    private long lastDamageTime = 0;
    private final int hurtCooldown = 500;
    private boolean deathComplete = false;
    private long deathEndTime = 0;
    private boolean visible = true;
    private int enemyWidth = 64;
    private int enemyHeight = 128;

    private int speed = 3;
    private boolean facingLeft = true;

    private enum State { IDLE, RUNNING, ATTACKING, DEFENDING, HURT, DEAD }
    private State currentState = State.IDLE;

    private Knight knight;

    private boolean inCooldown = false;
    private int attackCount = 0;
    private long lastAttackTime = 0;
    private final int attackCooldown = 2000; // 2 seconds

    public SkeletonWarrior(int x, int y) {
        this.x = x;
        this.y = y;
        loadFrames();
    }

    private void loadFrames() {
        idleFrames = loadSpriteSheet("assets/skeleton_warrior/skeleton_warrior_Idle.png", 7);
        walkFrames = loadSpriteSheet("assets/skeleton_warrior/skeleton_warrior_Walk.png", 8);
        attackFrames = loadSpriteSheet("assets/skeleton_warrior/skeleton_warrior_Attack.png", 5);
        hurtFrames = loadSpriteSheet("assets/skeleton_warrior/skeleton_warrior_Hurt.png", 2);
        deathFrames = loadSpriteSheet("assets/skeleton_warrior/skeleton_warrior_Death.png", 4);

        try {
            defendFrame = ImageIO.read(new File("assets/skeleton_warrior/skeleton_warrior_Defend.png"))
                    .getSubimage(0, 0, frameWidth, frameHeight);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private BufferedImage[] loadSpriteSheet(String path, int frameCount) {
        try {
            BufferedImage sheet = ImageIO.read(new File(path));
            BufferedImage[] frames = new BufferedImage[frameCount];
            for (int i = 0; i < frameCount; i++) {
                frames[i] = sheet.getSubimage(i * frameWidth, 0, frameWidth, frameHeight);
            }
            return frames;
        } catch (Exception e) {
            e.printStackTrace();
            return new BufferedImage[0];
        }
    }

    public void setSamurai(Knight knight) {
        this.knight = knight;
    }

    public void update() {
        if (knight == null) return;

        if (isDead && deathComplete && !visible) return; // stop processing forever

        int samuraiX = knight.getX();
        int distance = Math.abs(samuraiX - x);
        if (!isDead) {
            facingLeft = samuraiX < x;
        }

        if (isDead) {
            if (!deathComplete) {
                frameCounter++;
                if (frameCounter >= frameDelay) {
                    frameCounter = 0;
                    frameIndex++;
                    if (frameIndex >= deathFrames.length) {
                        frameIndex = deathFrames.length - 1;
                        deathComplete = true;
                        deathEndTime = System.currentTimeMillis();
                    }
                }
            } else {
                if (System.currentTimeMillis() - deathEndTime >= 4000) {
                    visible = false; // remove after 4 seconds
                }
            }
            return;
        }

        if (isHurt) {
            frameCounter++;
            if (frameCounter >= frameDelay) {
                frameCounter = 0;
                frameIndex++;
                if (frameIndex >= hurtFrames.length) {
                    isHurt = false;
                    frameIndex = 0;
                    currentState = State.IDLE;
                }
            }
            return;
        }

        if (stunned) {
            if (System.currentTimeMillis() - stunStartTime >= stunDuration) {
                stunned = false;
            } else {
                currentState = State.IDLE;
                frameIndex = 0;
                return;
            }
        }

        if (inCooldown && System.currentTimeMillis() - lastAttackTime >= attackCooldown) {
            inCooldown = false;
            attackCount = 0;
        }

        if (!inCooldown) {
            int samuraiY = knight.getY();

            int verticalThreshold = 40;
            int horizontalThreshold = 60;

            boolean samuraiNearby = Math.abs(samuraiX - x) <= horizontalThreshold &&
                    Math.abs(samuraiY - y) <= verticalThreshold;

            if (knight.isAttacking() && samuraiNearby) {
                startDefending();
            } else if (distance > 15) {
                startRunning();
                moveTowards(samuraiX);
            } else {
                if (currentState != State.ATTACKING && attackCount < 2) {
                    startAttacking();
                }
            }
        } else {
            if (currentState == State.DEFENDING && !knight.isAttacking()) {
                currentState = State.IDLE;
                frameIndex = 0;
            }
        }

        frameCounter++;
        if (frameCounter >= frameDelay) {
            frameCounter = 0;
            frameIndex++;

            switch (currentState) {
                case IDLE -> frameIndex %= idleFrames.length;
                case RUNNING -> frameIndex %= walkFrames.length;
                case ATTACKING -> {
                    if (frameIndex == attackFrames.length - 2) {
                        if (isAttackHittingSamurai(knight)) {
                            knight.takeDamage(20); // <-- Make sure this returns void or boolean accordingly
                            // Remove parry logic if Knight.takeDamage is void
                        }
                    }
                    if (frameIndex >= attackFrames.length) {
                        frameIndex = 0;
                        attackCount++;
                        if (attackCount >= 2) {
                            inCooldown = true;
                            lastAttackTime = System.currentTimeMillis();
                        }
                        currentState = State.IDLE;
                    }
                }
                case DEFENDING -> frameIndex = 0;
                default -> frameIndex = 0;
            }
        }
    }

    private void moveTowards(int targetX) {
        if (targetX < x) x -= speed;
        else if (targetX > x) x += speed;
    }

    public void startRunning() {
        if (currentState != State.RUNNING) {
            currentState = State.RUNNING;
            frameIndex = 0;
        }
    }

    public void startAttacking() {
        if (currentState != State.ATTACKING && !inCooldown) {
            currentState = State.ATTACKING;
            frameIndex = 0;
        }
    }

    public void startDefending() {
        if (currentState != State.DEFENDING) {
            currentState = State.DEFENDING;
            frameIndex = 0;
        }
    }

    private boolean isAttackHittingSamurai(Knight knight) {
        int attackRange = 30;

        Rectangle samuraiBounds = knight.getBounds();
        Rectangle attackBox;

        if (facingLeft) {
            attackBox = new Rectangle(x - attackRange, y, attackRange, frameHeight);
        } else {
            attackBox = new Rectangle(x + frameWidth, y, attackRange, frameHeight);
        }

        return attackBox.intersects(samuraiBounds);
    }

    public void draw(Graphics g) {
        if (!visible) return;

        BufferedImage currentFrame;

        if (isDead) {
            currentFrame = deathFrames[Math.min(frameIndex, deathFrames.length - 1)];
        } else if (isHurt) {
            currentFrame = hurtFrames[Math.min(frameIndex, hurtFrames.length - 1)];
        } else {
            switch (currentState) {
                case IDLE -> currentFrame = idleFrames[frameIndex % idleFrames.length];
                case RUNNING -> currentFrame = walkFrames[frameIndex % walkFrames.length];
                case ATTACKING -> currentFrame = attackFrames[frameIndex % attackFrames.length];
                case DEFENDING -> currentFrame = defendFrame;
                default -> currentFrame = idleFrames[0];
            }
        }

        if (facingLeft) {
            g.drawImage(flipImage(currentFrame), x, y, null);
        } else {
            g.drawImage(currentFrame, x, y, null);
        }
    }

    public void takeDamage(int damage) {
        long now = System.currentTimeMillis();
        if (now - lastDamageTime > hurtCooldown && !isDead) {
            health -= damage;
            lastDamageTime = now;
            if (health <= 0) {
                isDead = true;
                frameIndex = 0;
                currentState = State.DEAD;
            } else {
                isHurt = true;
                frameIndex = 0;
                currentState = State.HURT;
            }
        }
    }

    public boolean isDead() {
        return isDead;
    }

    public boolean isVisible() {
        return visible;
    }

    private BufferedImage flipImage(BufferedImage img) {
        BufferedImage flipped = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
        Graphics2D g2d = flipped.createGraphics();
        g2d.drawImage(img, img.getWidth(), 0, -img.getWidth(), img.getHeight(), null);
        g2d.dispose();
        return flipped;
    }
    public void setX(int x) {
    	this.x = x;
    	}
    public int getX() { return x; }
    public int getY() { return y; }

    public Rectangle getBounds() {
        return new Rectangle(x, y, enemyWidth, enemyHeight);
    }
}
