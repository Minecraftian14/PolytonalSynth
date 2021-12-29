package in.mcxiv.old.synth;

import in.mcxiv.old.synth.io.SoundSpecialization;
import in.mcxiv.old.synth.tone.Tone;
import in.mcxiv.old.synth.tone.TonePressState;

import javax.sound.sampled.LineUnavailableException;
import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.concurrent.atomic.AtomicInteger;

class AHellLottaBetterTest {
    public static void main(String[] args) throws LineUnavailableException, InterruptedException {
        AtomicInteger i = new AtomicInteger(440);
        Environment environment = new Environment(new Tone() {
            @Override
            public double calculate(double t) {
                return Math.sin(i.get() * t);
            }
        });

        new JFrame() {{
            setVisible(true);
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            setSize(500, 500);
            addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_UP) i.incrementAndGet();
                    else if (e.getKeyCode() == KeyEvent.VK_DOWN) i.decrementAndGet();
                    else if (e.getKeyCode() == KeyEvent.VK_SPACE)
                        if (environment.state == TonePressState.INACTIVE) environment.trigger();
                    System.out.println("e.getKeyCode() = " + e.getKeyCode());
                }

                @Override
                public void keyReleased(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_SPACE)
                        if (environment.state != TonePressState.INACTIVE) environment.de_trigger();
                    System.out.println("e.getKeyCode() = " + e.getKeyCode());
                }
            });
            setLocationRelativeTo(null);
        }};

        try (SoundSpecialization sp = new SoundSpecialization(environment)) {
            sp.init();
            long cur = System.currentTimeMillis();
            while (System.currentTimeMillis() - cur < 60_000)
                sp.playStep();
        }
    }
}