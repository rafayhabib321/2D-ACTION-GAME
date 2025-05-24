package base;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Samurai {
private int x = 100, y = 400;
private int normalSpeed = 5;
private int defendSpeed = 2;
private boolean left, right, jumping, defending;
private int yVelocity = 0;
private final int gravity = 1;
private boolean facingLeft = false;

private BufferedImage[] walkFrames;
private BufferedImage[] idleFrames;
private BufferedImage[] atk1Frames;
private BufferedImage[] atk2Frames;
private BufferedImage[] jumpFrames;
private BufferedImage defendFrame;

private int frameIndex = 0;
private int frameTimer = 0;
private int frameDelay = 6; // default
private int attackFrameDelay = 3;

private enum State { IDLE, WALKING, JUMPING, ATTACK1, ATTACK2, DEFENDING }
private State currentState = State.IDLE;

private boolean attackToggle = false;
private boolean isAttacking = false;

public Samurai() {
    loadSprites();
}

private void loadSprites() {
    walkFrames = loadFrames("assets/samurai_walk.png", 8);
    idleFrames = loadFrames("assets/samurai_idle.png", 4); // NEW
    atk1Frames = loadFrames("assets/samurai_atk1.png", 5);
    atk2Frames = loadFrames("assets/samurai_atk2.png", 4);
    jumpFrames = loadFrames("assets/samurai_jump.png", 6);
    BufferedImage[] defendFrames = loadFrames("assets/samurai_defend.png", 6);
    defendFrame = defendFrames[0];
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

    if (!isAttacking) {
        if (left) {
            x -= speed;
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
}

private void updateAnimation() {
    frameTimer++;

    int delay = (isAttacking) ? attackFrameDelay : frameDelay;

    if (frameTimer >= delay) {
        frameTimer = 0;
        frameIndex++;

        if (isAttacking) {
            BufferedImage[] attackFrames = (currentState == State.ATTACK1) ? atk1Frames : atk2Frames;
            if (frameIndex >= attackFrames.length) {
                isAttacking = false;
                frameIndex = 0;
                if (jumping) {
                    currentState = State.JUMPING;
                } else if (left || right) {
                    currentState = State.WALKING;
                } else if (defending) {
                    currentState = State.DEFENDING;
                } else {
                    currentState = State.IDLE;
                }
            }
        } else {
            switch (currentState) {
                case WALKING:
                    if (frameIndex >= walkFrames.length) frameIndex = 0;
                    break;
                case JUMPING:
                    if (frameIndex >= jumpFrames.length) frameIndex = jumpFrames.length - 1;
                    break;
                case IDLE:
                    if (frameIndex >= idleFrames.length) frameIndex = 0;
                    break;
            }
        }
    }

    if (!isAttacking) {
        if (jumping) {
            currentState = State.JUMPING;
        } else if (defending) {
            currentState = State.DEFENDING;
            frameIndex = 0;
        } else if (left || right) {
            currentState = State.WALKING;
        } else {
            currentState = State.IDLE;
        }
    }
}

public void draw(Graphics g) {
    BufferedImage currentFrame = null;

    switch (currentState) {
        case WALKING -> currentFrame = walkFrames[frameIndex % walkFrames.length];
        case JUMPING -> currentFrame = jumpFrames[frameIndex % jumpFrames.length];
        case ATTACK1 -> currentFrame = atk1Frames[frameIndex % atk1Frames.length];
        case ATTACK2 -> currentFrame = atk2Frames[frameIndex % atk2Frames.length];
        case DEFENDING -> currentFrame = defendFrame;
        case IDLE -> currentFrame = idleFrames[frameIndex % idleFrames.length];
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
    this.left = pressed;
}

public void moveRight(boolean pressed) {
    this.right = pressed;
}

public void jump() {
    if (!jumping) {
        jumping = true;
        yVelocity = -15;
        frameIndex = 0;
    }
}

public void attack() {
    if (!isAttacking) {
        isAttacking = true;
        attackToggle = !attackToggle;
        currentState = attackToggle ? State.ATTACK1 : State.ATTACK2;
        frameIndex = 0;
        frameTimer = 0;
    }
}

public void setDefending(boolean defending) {
    this.defending = defending;
    if (defending && !isAttacking) {
        currentState = State.DEFENDING;
        frameIndex = 0;
    }
}
}