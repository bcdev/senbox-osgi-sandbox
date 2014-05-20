package org.teo.launcher.splash;

import org.teo.launcher.Launcher;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

public class SplashScreenFactory {

    public static SplashScreen createSplashScreen(Properties config) {
        boolean headless = config.getProperty(Launcher.TEO_HEADLESS, "false").equalsIgnoreCase("true");
        if (!headless) {
            String property = config.getProperty(Launcher.TEO_SPLASHSCREEN_IMAGE);
            URL imageUrl = null;
            if (property != null) {
                try {
                    imageUrl = new URL(property);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            } else {
                imageUrl = SplashScreenFactory.class.getResource("splash.jpg");
            }
            if (imageUrl != null) {
                return new SplashScreenImpl(imageUrl);
            }
        }
        return SplashScreen.NONE;
    }
}