package SoundTests.Onnesaan;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.atomic.AtomicBoolean;

public class UI {

    public static void main(String[] args) throws Exception {
        UI ui = new UI();
        TheInstrument instrument = new TheInstrument();
        instrument.init();


        AtomicBoolean isOn = new AtomicBoolean(true);
        new Thread(() -> {
            try {
                while (isOn.get()) {
                    instrument.playStep();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();


        ui.frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                isOn.set(false);
                try {
                    instrument.close();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        });
        ui.btn_inc.addActionListener(e -> instrument.incTarget());
        ui.btn_dec.addActionListener(e -> instrument.decTarget());

    }

    JFrame frame;
    JPanel pan_graph;
    JButton btn_inc;
    JButton btn_dec;

    public UI() {

        frame = new JFrame("The Instrument");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        GridBagLayout layout = new GridBagLayout();
        frame.setLayout(layout);

        frame.add(pan_graph = new JPanel(), getOne(0, 0));
        frame.add(btn_dec = new JButton("Inc"), getOne(0, 1));
        frame.add(btn_inc = new JButton("Dec"), getOne(1, 1));

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

    }

    private static GridBagConstraints getOne(int x, int y) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = x;
        constraints.gridy = y;
        if (x == 0 && y == 0) constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(y > 1 ? 0 : 10, x > 1 ? 0 : 10, 10, 10);
        return constraints;
    }

}
