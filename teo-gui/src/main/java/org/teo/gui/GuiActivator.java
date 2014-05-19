package org.teo.gui;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.ServiceRegistration;
import org.teo.gui.impl.GuiImpl;

public class GuiActivator implements BundleActivator, FrameworkListener {

    private GuiImpl gui;
    private ServiceRegistration<?> guiServiceRegistration;

    @Override
    public void start(final BundleContext bundleContext) {
        gui = new GuiImpl(bundleContext);
        gui.init();

        guiServiceRegistration = bundleContext.registerService(Gui.class.getName(), gui, null);
        bundleContext.addFrameworkListener(this);
    }

    @Override
    public void stop(BundleContext bundleContext) {
        bundleContext.removeFrameworkListener(this);
        guiServiceRegistration.unregister();

        gui = null;
    }

    @Override
    public void frameworkEvent(FrameworkEvent event) {
        if (event.getType() == FrameworkEvent.STARTED) {
            gui.run();
        }
    }
}