package in.mcxiv;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class UIj {

    public static void main(String[] args) {
        new UIj(new CallBackDevice() {
            @Override
            public void down(int id) {
                System.out.println("id d = " + id);
            }

            @Override
            public void up(int id) {
                System.out.println("id u = " + id);
            }
        });
    }

    public UIj(CallBackDevice device) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        List<Integer> list = new ArrayList<>(128);
        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();
                if (list.contains(keyCode)) return;
                list.add(keyCode);
                device.down(keyCode);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                int keyCode = e.getKeyCode();
                if (!list.contains(keyCode)) return;
                list.remove(((Object) keyCode));
                device.up(keyCode);
            }
        });

        frame.setSize(500, 500);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public interface CallBackDevice {
        void down(int id);

        void up(int id);
    }

}
