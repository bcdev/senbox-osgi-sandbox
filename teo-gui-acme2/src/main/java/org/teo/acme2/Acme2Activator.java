package org.teo.acme2;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.teo.gui.Gui;
import org.teo.gui.command.CommandGroup;

public class Acme2Activator implements BundleActivator {

    private Gui gui;
    private ServiceReference guiRerviceReference;

    @Override
    public void start(BundleContext bundleContext) {
        guiRerviceReference = bundleContext.getServiceReference(Gui.class.getName());
        if (guiRerviceReference != null) {
            gui = (Gui) bundleContext.getService(guiRerviceReference);
            CommandGroup group = gui.getCommandGroup("view");
            group.addCommand(new Acme2Command(gui));
        } else {
            System.out.println(getClass().getName() + ": missing service " + Gui.class.getName());
        }
    }

    @Override
    public void stop(BundleContext bundleContext) {
        if (gui != null) {
            CommandGroup group = gui.getCommandGroup("view");
            group.removeCommand(Acme2Command.ID);
        }
        if (guiRerviceReference != null) {
            bundleContext.ungetService(guiRerviceReference);
        }
    }
}