package base;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Knight {
    private int x = 100, y = 400;
    private int normalSpeed = 5;
    private int defendSpeed = 2;
    private boolean left, right, jumping, defending;
    private int yVelocity = 0;
    private final int gravity = 1;
    private boolean facingLeft = false;
    private int health = 1000;
    private long lastDamageTime = 0;
    private final int damageCooldown = 500; // milliseconds
    private boolean isDead = false;
    private boolean deathComplete = false;
    private static final int HIT_FRAME_INDEX = 2;

    private BufferedImage[] walkFrames;
    private BufferedImage[] idleFrames;
    private BufferedImage[] atk1Frames;
    private BufferedImage[] atk2Frames;
    private BufferedImage[] jumpFrames;
    private BufferedImage[] hurtFrames;
    private BufferedImage defendFrame;
    private BufferedImage[] deathFrames;

    private int frameIndex = 0;
    private int frameTimer = 0;
    private int frameDelay = 6;
    private int attackFrameDelay = 3;

    private enum State { IDLE, WALKING, JUMPING, ATTACK1, ATTACK2, DEFENDING, HURT, DEATH }
    private State currentState = State.IDLE;

    private boolean attackToggle = false;
    private boolean isAttacking = false;
    private boolean isHurt = false;

    public Knight() {
        loadSprites();
    }

    private void loadSprites() {
        walkFrames = loadFrames("assets/knight/knight_walk.png", 8);
        idleFrames = loadFrames("assets/knight/knight_idle.png", 4);
        atk1Frames = loadFrames("assets/knight/knight_atk1.png", 5);
        atk2Frames = loadFrames("assets/knight/knight_atk2.png", 4);
        jumpFrames = loadFrames("assets/knight/knight_jump.png", 6);
        hurtFrames = loadFrames("assets/knight/knight_hurt.png", 4);
        deathFrames = loadFrames("assets/knight/knight_death.png", 6);

        try {
            BufferedImage defendSheet = ImageIO.read(new File("assets/knight/knight_defend.png"));
            int frameWidth = defendSheet.getWidth() / 6;
            int frameHeight = defendSheet.getHeight();
            defendFrame = defendSheet.getSubimage(0, 0, frameWidth, frameHeight);
        } catch (IOException e) {
            e.printStackTrace();
            defendFrame = null;
        }
    }

    private BufferedImage[] loadFrames(String path, int count) {
        try {
            BufferedImage sheet = ImageIO.read(new File(path));
            int frameWidth = sheet.getWidth() / count;
            int frameHeight = sheet.getHeight();
            BufferedImage[] frames = new BufferedImage[count];
            for (int i = 0; i < count; i++) {
                frames[i] = sheet.getSubimage(i * frameWidth, 0, frameWidth, frameHeight);
            }
            return frames;
        } catch (IOException e) {
            e.printStackTrace();
            return new BufferedImage[0];
        }
    }

    public void update() {
        int speed = defending ? defendSpeed : normalSpeed;

        if (isDead) {
            updateAnimation();
            return;
        }

        if (!isAttacking && !isHurt) {
            if (left) {
                x -= speed;
                if (x < 0) x = 0;
                facingLeft = true;
            }
            if (right) {
                x += speed;
                facingLeft = false;
            }
        }

        if (jumping) {
            y += yVelocity;
            yVelocity += gravity;
            if (y >= 400) {
                y = 400;
                yVelocity = 0;
                jumping = false;
            }
        }

        updateAnimation();

        if (isHurt && currentState != State.HURT) {
            isHurt = false;
        }
    }

    private void updateAnimation() {
        frameTimer++;
        int delay = (isAttacking || isHurt) ? attackFrameDelay : frameDelay;

        if (isDead) {
            currentState = State.DEATH;
            if (frameTimer >= delay) {
                frameTimer = 0;
                frameIndex++;
                if (frameIndex >= deathFrames.length) {
                    frameIndex = deathFrames.length - 1;
                    deathComplete = true;
                }
            }
            return;
        }

        if (frameTimer >= delay) {
            frameTimer = 0;
            frameIndex++;

            if (isAttacking) {
                BufferedImage[] attackFrames = (currentState == State.ATTACK1) ? atk1Frames : atk2Frames;
                if (frameIndex >= attackFrames.length) {
                    isAttacking = false;
                    frameIndex = 0;
                    resolvePostAnimationState();
                }
            } else if (isHurt) {
                if (frameIndex >= hurtFrames.length) {
                    isHurt = false;
                    frameIndex = 0;
                    resolvePostAnimationState();
                }
            } else {
                switch (currentState) {
                    case WALKING -> {
                        if (frameIndex >= walkFrames.length) frameIndex = 0;
                    }
                    case JUMPING -> {
                        if (frameIndex >= jumpFrames.length) frameIndex = jumpFrames.length - 1;
                    }
                    case IDLE -> {
                        if (frameIndex >= idleFrames.length) frameIndex = 0;
                    }
                }
            }
        }

        if (!isAttacking && !isHurt && !isDead) {
            if (jumping) currentState = State.JUMPING;
            else if (defending) {
                currentState = State.DEFENDING;
                frameIndex = 0;
            } else if (left || right) currentState = State.WALKING;
            else currentState = State.IDLE;
        }
    }

    private void resolvePostAnimationState() {
        if (jumping) currentState = State.JUMPING;
        else if (defending) currentState = State.DEFENDING;
        else if (left || right) currentState = State.WALKING;
        else currentState = State.IDLE;
    }

    public void draw(Graphics g) {
        if (isDead && deathComplete) return;

        BufferedImage currentFrame = null;

        switch (currentState) {
            case WALKING -> currentFrame = walkFrames[frameIndex % walkFrames.length];
            case JUMPING -> currentFrame = jumpFrames[frameIndex % jumpFrames.length];
            case ATTACK1 -> currentFrame = atk1Frames[frameIndex % atk1Frames.length];
            case ATTACK2 -> currentFrame = atk2Frames[frameIndex % atk2Frames.length];
            case DEFENDING -> currentFrame = defendFrame;
            case HURT -> currentFrame = hurtFrames[frameIndex % hurtFrames.length];
            case IDLE -> currentFrame = idleFrames[frameIndex % idleFrames.length];
            case DEATH -> currentFrame = deathFrames[frameIndex % deathFrames.length];
        }

        if (currentFrame != null) {
            if (facingLeft) {
                g.drawImage(flipImage(currentFrame), x, y, null);
            } else {
                g.drawImage(currentFrame, x, y, null);
            }
        }
    }

    private BufferedImage flipImage(BufferedImage img) {
        BufferedImage flipped = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
        Graphics2D g2d = flipped.createGraphics();
        g2d.drawImage(img, img.getWidth(), 0, -img.getWidth(), img.getHeight(), null);
        g2d.dispose();
        return flipped;
    }

    public void moveLeft(boolean pressed) {
        if (isDead) return;
        this.left = pressed;
    }

    public void moveRight(boolean pressed) {
        if (isDead) return;
        this.right = pressed;
    }

    public void jump() {
        if (isDead) return;
        if (!jumping) {
            jumping = true;
            yVelocity = -15;
            frameIndex = 0;
        }
    }

    public void attack() {
        if (isDead) return;
        if (!isAttacking && !isHurt) {
            isAttacking = true;
            attackToggle = !attackToggle;
            currentState = attackToggle ? State.ATTACK1 : State.ATTACK2;
            frameIndex = 0;
            frameTimer = 0;
        }
    }

    public void setDefending(boolean defending) {
        if (isDead) return;
        this.defending = defending;
        if (defending && !isAttacking && !isHurt) {
            currentState = State.DEFENDING;
            frameIndex = 0;
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, 64, 128);
    }

    public Rectangle getAttackBox() {
        int attackWidth = 40;
        int attackHeight = 128;
        int attackX = facingLeft ? x - attackWidth : x + getWidth();
        return new Rectangle(attackX, y, attackWidth, attackHeight);
    }

    public boolean takeDamage(int damage) {
        long currentTime = System.currentTimeMillis();
        if (isDead || isHurt) return false;

        if (defending && currentTime - lastDamageTime > damageCooldown) {
            lastDamageTime = currentTime;
            System.out.println("Perfect Parry!");
            return true;
        }

        if (currentTime - lastDamageTime > damageCooldown) {
            health -= damage;
            lastDamageTime = currentTime;

            if (health <= 0) {
                health = 0;
                isDead = true;
                currentState = State.DEATH;
                frameIndex = 0;
                frameTimer = 0;
                System.out.println("Knight defeated!");
            } else {
                isHurt = true;
                currentState = State.HURT;
                frameIndex = 0;
                frameTimer = 0;
            }
        }
        return false;
    }

    public boolean isInAttackHitFrame() {
        return (currentState == State.ATTACK1 || currentState == State.ATTACK2) && frameIndex == HIT_FRAME_INDEX;
    }

    public boolean isFacingLeft() {
        return facingLeft;
    }

    public int getWidth() {
        return 64;
    }

    public int getHeight() {
        return 128;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public boolean isAttacking() {
        return isAttacking;
    }

    public boolean isDefending() {
        return defending;
    }

    public int getHealth() {
        return health;
    }

    public boolean isDead() {
        return isDead;
    }
}
