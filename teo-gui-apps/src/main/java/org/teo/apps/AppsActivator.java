package org.teo.apps;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.teo.gui.Gui;
import org.teo.gui.command.CommandGroup;

/**
 * Created by Norman on 20.02.14.
 */
public class AppsActivator implements BundleActivator {

    private static AppsTracker appsTracker;
    private ServiceReference guiServiceReference;
    private Gui gui;

    public static AppsTracker getAppsTracker() {
        return appsTracker;
    }

    @Override
    public void start(BundleContext bundleContext) {
        appsTracker = new AppsTracker();
        appsTracker.init(bundleContext);

        System.out.println("bundle started: " + bundleContext.getBundle());
        guiServiceReference = bundleContext.getServiceReference(Gui.class.getName());
        if (guiServiceReference != null) {
            gui = (Gui) bundleContext.getService(guiServiceReference);
            CommandGroup group = gui.getCommandGroup("tools");
            group.addCommand(new AppsCommand(gui));
        } else {
            System.out.println(AppsActivator.class.getName() + ": missing service " + Gui.class.getName());
        }
    }

    @Override
    public void stop(BundleContext bundleContext) {
        if (gui != null) {
            CommandGroup group = gui.getCommandGroup("tools");
            group.removeCommand(AppsCommand.ID);
        }
        gui = null;
        if (guiServiceReference != null) {
            bundleContext.ungetService(guiServiceReference);
        }
        appsTracker = null;
    }
}