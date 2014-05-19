package org.teo.splash;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.net.URL;

public class SplashScreen {

    private JWindow window;

    public void open() {
        if (window != null) {
            return;
        }

        final URL imageResource = getClass().getResource("splash.jpg");

        window = new JWindow();
        window.add(new JLabel(new ImageIcon(imageResource)), BorderLayout.CENTER);
        window.pack();
        Dimension windowSize = window.getSize();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        window.setLocation((screenSize.width - windowSize.width) / 2, (screenSize.height - windowSize.height) / 2);
        window.setAlwaysOnTop(true);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                window.setVisible(true);
                System.out.println("splash screen opened");
            }
        });
    }

    public void close() {
        if (window == null) {
            return;
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                window.dispose();
                window = null;
                System.out.println("splash screen closed");
            }
        });
    }
}
