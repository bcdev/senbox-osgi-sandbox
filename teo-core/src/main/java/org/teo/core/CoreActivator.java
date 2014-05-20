package org.teo.core;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * @author Norman Fomferra
 */
public class CoreActivator implements BundleActivator {

    private ServiceRegistration<OperatorRegistry> serviceRegistration;

    @Override
    public void start(BundleContext context) throws Exception {
        serviceRegistration = context.registerService(OperatorRegistry.class, OperatorRegistry.INSTANCE, null);
        System.out.println("CoreActivator: serviceRegistration = " + serviceRegistration);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        serviceRegistration.unregister();
    }
}
