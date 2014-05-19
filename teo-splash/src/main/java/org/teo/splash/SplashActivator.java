package org.teo.splash;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;

import javax.swing.JOptionPane;

public class SplashActivator implements BundleActivator, FrameworkListener {

    SplashScreen splashScreen;

    @Override
    public void start(BundleContext bundleContext) {
        System.out.println("bundle started: " + bundleContext.getBundle());
        bundleContext.addFrameworkListener(this);
        splashScreen = createSplash();
        splashScreen.open();
    }

    @Override
    public void stop(BundleContext bundleContext) {
        System.out.println("bundle stopped: " + bundleContext.getBundle());
        splashScreen.close();
        bundleContext.removeFrameworkListener(this);
    }

    @Override
    public void frameworkEvent(FrameworkEvent event) {
        System.out.println("frameworkEvent: " + event);
        if (event.getType() == FrameworkEvent.STARTED) {
            splashScreen.close();
        } else if (event.getType() == FrameworkEvent.ERROR) {
            splashScreen.close();
            Throwable throwable = event.getThrowable();
            String message;
            if (throwable != null) {
                throwable.printStackTrace(System.err);
                message = throwable.getMessage();
            } else {
                message = "Unknown problem.";
            }
            JOptionPane.showMessageDialog(null, message, "OSGi Framework Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    SplashScreen createSplash() {
        return new SplashScreen();
    }

}