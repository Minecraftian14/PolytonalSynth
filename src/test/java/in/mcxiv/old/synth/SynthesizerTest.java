package in.mcxiv.old.synth;

import in.mcxiv.app.synth.Input;
import in.mcxiv.old.synth.io.SoundSpecialization;
import in.mcxiv.old.synth.tone.Tone;

import javax.sound.sampled.LineUnavailableException;
import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.concurrent.atomic.AtomicInteger;

class SynthesizerTest {
    public static void main(String[] args) throws LineUnavailableException, InterruptedException {
        AtomicInteger i = new AtomicInteger(440);

        EnvironmentNeo environment = new EnvironmentNeo();
        Tone sinTone = new Tone() {
            @Override
            public double calculate(double t, double f) {
                return Math.sin(f * t);
            }
        };

        Synthesizer synthesizer = new Synthesizer(sinTone, environment);

        new JFrame() {{
            setVisible(true);
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            setSize(500, 500);
            addKeyListener(new KeyAdapter() {
                Input input=null;

                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_UP) i.incrementAndGet();
                    else if (e.getKeyCode() == KeyEvent.VK_DOWN) i.decrementAndGet();
                    else if (e.getKeyCode() == KeyEvent.VK_SPACE) if (input == null)
                        synthesizer.submit(input = new Input() {{
                            frequency = i.get();
                        }});
//                    System.out.println("e.getKeyCode() = " + e.getKeyCode());
                }

                @Override
                public void keyReleased(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_SPACE)
                        if (input != null) {
                            input.active = false;
                            input = null;
                        }
                    System.out.println("e.ge Ke Cod () = " + e.getKeyCode());
                }
            });
            setLocationRelativeTo(null);
        }};

        try (SoundSpecialization sp = new SoundSpecialization(synthesizer)) {
            sp.init();
            long cur = System.currentTimeMillis();
            while (System.currentTimeMillis() - cur < 60_000)
                sp.playStep();
        }
    }
}