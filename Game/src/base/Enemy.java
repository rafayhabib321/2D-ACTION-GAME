package base;

import java.awt.*;

public interface Enemy {
    void update();
    void draw(Graphics g);
    void takeDamage(int damage);
    boolean isDead();
    boolean isVisible();
    Rectangle getBounds();
    int getX();
    int getY();
    
    // 🔧 Add these so you can call them from GamePanel
    void setX(int x);
    void setSamurai(Knight knight);
}
