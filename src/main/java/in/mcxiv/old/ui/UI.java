package in.mcxiv.old.ui;

import javax.swing.*;
import java.awt.*;

public class UI {

    public static void main(String[] args) {
        new UI();
    }

    public JFrame frm;
    public GridBagLayout lay_frm;

    public UI() {
        frm = new JFrame();
        frm.setLayout(lay_frm = new GridBagLayout());
        frm.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);


        frm.pack();
        frm.setLocationRelativeTo(null);
        frm.setVisible(true);
    }
}
