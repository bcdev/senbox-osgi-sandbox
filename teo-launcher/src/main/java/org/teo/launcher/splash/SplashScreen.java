package org.teo.launcher.splash;

import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;

/**
 * @author Norman Fomferra
 */
public interface SplashScreen extends FrameworkListener {
    SplashScreen NONE = new SplashScreen() {
        @Override
        public void open() {
        }

        @Override
        public void close() {
        }

        @Override
        public void frameworkEvent(FrameworkEvent event) {
        }
    };

    void open();

    void close();
}
