package org.teo;

import org.teo.launcher.Launcher;

/**
 * Represents Teo's GUI (actually it waits until the 'teo-gui' bundle starts its GUI after the framework started.
 */
public class TeoGui {
    public static void main(String[] args) throws Exception {
        System.setProperty(Launcher.TEO_HEADLESS, "false");
        System.setProperty(Launcher.TEO_MODULES_WATCHER, "true");

        Launcher.start();
    }
}

