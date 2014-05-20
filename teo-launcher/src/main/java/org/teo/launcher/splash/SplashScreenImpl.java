package org.teo.launcher.splash;

import org.osgi.framework.FrameworkEvent;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.net.URL;

public class SplashScreenImpl implements SplashScreen {

    private URL imageUrl;
    private JWindow window;

    public SplashScreenImpl(URL imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public void open() {
        if (window == null) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    window = new JWindow();
                    window.add(new JLabel(new ImageIcon(imageUrl)), BorderLayout.CENTER);
                    window.pack();
                    Dimension windowSize = window.getSize();
                    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                    window.setLocation((screenSize.width - windowSize.width) / 2, (screenSize.height - windowSize.height) / 2);
                    window.setAlwaysOnTop(true);
                    window.setVisible(true);
                    System.out.println("Teo's splash screen opened");
                }
            });
        }
    }

    @Override
    public void close() {
        if (window != null) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (window != null) {
                        window.dispose();
                        window = null;
                        System.out.println("Teo's splash screen closed");
                    }
                }
            });
        }
    }

    @Override
    public void frameworkEvent(FrameworkEvent event) {
        if (window != null) {
            if (event.getType() == FrameworkEvent.STARTED
                || event.getType() == FrameworkEvent.STOPPED
                || event.getType() == FrameworkEvent.ERROR) {
                close();
            }
        }
    }
}
