package org.teo.acme1;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.teo.gui.Gui;
import org.teo.gui.command.CommandGroup;

public class Acme1Activator implements BundleActivator {

    private Gui gui;
    private ServiceReference guiServiceReference;

    @Override
    public void start(BundleContext bundleContext) {
        guiServiceReference = bundleContext.getServiceReference(Gui.class.getName());
        if (guiServiceReference != null) {
            gui = (Gui) bundleContext.getService(guiServiceReference);
            CommandGroup group = gui.getCommandGroup("view");
            group.addCommand(new Acme1Command(gui));
        } else {
            System.out.println(Acme1Activator.class.getName() + ": missing service " + Gui.class.getName());
        }
    }

    @Override
    public void stop(BundleContext bundleContext) {
        System.out.println("bundle stopped: " + bundleContext.getBundle());
        if (gui != null) {
            CommandGroup group = gui.getCommandGroup("view");
            group.removeCommand(Acme1Command.ID);
        }
        if (guiServiceReference != null) {
            bundleContext.ungetService(guiServiceReference);
        }
    }
}