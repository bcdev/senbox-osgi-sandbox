package org.teo;

import org.osgi.framework.Constants;
import org.teo.launcher.Launcher;

/**
 * Represents Teo's command line interface.
 */
public class TeoCli {
    public static void main(String[] args) throws Exception {
        System.setProperty(Launcher.TEO_HEADLESS, "true");
        System.setProperty(Launcher.TEO_MODULES_WATCHER, "false");
        System.setProperty(Constants.FRAMEWORK_BEGINNING_STARTLEVEL, "None");
        System.setProperty(Constants.FRAMEWORK_BEGINNING_STARTLEVEL, "None");

        int sleepSecs = args.length == 1 ? Integer.parseInt(args[0]) : 4;
        long sleepMillis = sleepSecs * 1000L;
        Launcher launcher = Launcher.start();

        int steps = 100;
        for (int step = 1; step <= steps; step++) {
            Thread.sleep(sleepMillis / steps);
            System.out.println("Processing " + step + "% done");
        }
        System.out.println("Processing finished");

        launcher.stopAndWait();
    }
}

