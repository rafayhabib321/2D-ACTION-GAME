package base;
import java.awt.event.*;

public class InputHandler extends KeyAdapter {
    private Knight knight;

    public InputHandler(Knight knight) {
        this.knight = knight;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_A:
                knight.moveLeft(true);
                break;
            case KeyEvent.VK_D:
                knight.moveRight(true);
                break;
            case KeyEvent.VK_W:
                knight.jump();
                break;
            case KeyEvent.VK_Z:
                knight.attack();
                break;
            case KeyEvent.VK_X:
                knight.setDefending(true);
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_A:
                knight.moveLeft(false);
                break;
            case KeyEvent.VK_D:
                knight.moveRight(false);
                break;
            case KeyEvent.VK_X:
                knight.setDefending(false);
                break;
        }
    }
}