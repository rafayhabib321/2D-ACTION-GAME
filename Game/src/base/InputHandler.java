package base;
import java.awt.event.*;

public class InputHandler extends KeyAdapter {
    private Samurai samurai;

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