package base;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Arrow {
    private int x, y;
    private final int speed = 8; // pixels per update
    private boolean active = true;
    private boolean facingLeft;
    private BufferedImage image;
    private Rectangle hitbox;

    private final int width;
    private final int height;

    public Arrow(int x, int y, boolean facingLeft, BufferedImage image) {
        this.x = x;
        this.y = y;
        this.facingLeft = facingLeft;
        this.image = image;
        this.width = image.getWidth();
        this.height = image.getHeight();
        this.hitbox = new Rectangle(x, y, width, height);
    }

    public void update() {
        // Move the arrow
        x += facingLeft ? -speed : speed;

        // Update the hitbox position
        hitbox.setLocation(x, y);

        // Deactivate the arrow if it goes off-screen
        if (x < -width || x > 2000) { // Adjust right limit to your game world's width
            active = false;
        }
    }

    public void draw(Graphics g, int cameraX) {
        int drawX = x - cameraX;

        if (facingLeft) {
            g.drawImage(flipImage(image), drawX, y, null);
        } else {
            g.drawImage(image, drawX, y, null);
        }

        // Optional: Draw hitbox for debugging
        // g.setColor(Color.RED);
        // g.drawRect(hitbox.x - cameraX, hitbox.y, hitbox.width, hitbox.height);
    }

    private BufferedImage flipImage(BufferedImage img) {
        BufferedImage flipped = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
        Graphics2D g2d = flipped.createGraphics();
        g2d.drawImage(img, img.getWidth(), 0, -img.getWidth(), img.getHeight(), null);
        g2d.dispose();
        return flipped;
    }

    public Rectangle getBounds() {
        return hitbox;
    }

    public boolean isActive() {
        return active;
    }

    public void deactivate() {
        active = false;
    }
}
