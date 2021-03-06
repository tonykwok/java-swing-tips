package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private Wipe mode = Wipe.In;

    public MainPanel() {
        super(new BorderLayout());
        Timer animator = new Timer(5, null);
        ImageIcon icon = new ImageIcon(getClass().getResource("test.png"));
        JComponent wipe = new JComponent() {
            protected int ww;
            @Override protected void paintComponent(Graphics g) {
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth(), getHeight());
                switch (getWipeMode()) {
                  case In:
                    if (ww < icon.getIconWidth()) {
                        ww += 10;
                    } else {
                        animator.stop();
                    }
                    break;
                  case Out:
                  default:
                    if (ww > 0) {
                        ww -= 10;
                    } else {
                        animator.stop();
                    }
                    break;
                }
                int iw = icon.getIconWidth();
                int ih = icon.getIconHeight();
                g.drawImage(icon.getImage(), 0, 0, iw, ih, this);
                g.fillRect(ww, 0, iw, ih);
            }
        };
        wipe.setBackground(Color.BLACK);
        animator.addActionListener(e -> wipe.repaint());

        JButton button1 = new JButton("Wipe In");
        button1.addActionListener(e -> {
            setWipeMode(Wipe.In);
            animator.start();
        });

        JButton button2 = new JButton("Wipe Out");
        button2.addActionListener(e -> {
            setWipeMode(Wipe.Out);
            animator.start();
        });

        add(wipe);
        add(button1, BorderLayout.SOUTH);
        add(button2, BorderLayout.NORTH);
        setOpaque(false);
        setPreferredSize(new Dimension(320, 240));
        animator.start();
    }
    protected void setWipeMode(Wipe mode) {
        this.mode = mode;
    }
    protected Wipe getWipeMode() {
        return mode;
    }
    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

enum Wipe { In, Out }
