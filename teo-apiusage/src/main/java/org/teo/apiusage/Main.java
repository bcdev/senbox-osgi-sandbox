package org.teo.apiusage;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;
import org.teo.core.OperatorRegistry;
import org.teo.launcher.Launcher;

/**
 * An example that uses the API exposed by 'teo-core'.
 *
 * @author Norman Fomferra
 */
public class Main {
    public static void main(String[] args) throws Exception {
        OperatorRegistry operatorRegistry1 = OperatorRegistry.INSTANCE;
        System.out.println("operatorRegistry1 = " + operatorRegistry1);
        for (String opName : operatorRegistry1.getOperatorSpis().keySet()) {
            System.out.println("  opName: " + opName);
        }

        System.setProperty(Launcher.TEO_HEADLESS, "true");
        System.setProperty(Launcher.TEO_MODULES_WATCHER, "false");
        System.setProperty(Constants.FRAMEWORK_BEGINNING_STARTLEVEL, "3");
        //System.setProperty("org.osgi.framework.system.packages.extra", "org.teo.core;version=0.1.0");
        Launcher launcher = Launcher.start();

        Framework framework = launcher.getFramework();
        while (framework.getState() != Bundle.ACTIVE) {
            Thread.currentThread().sleep(100);
        }

        BundleContext bundleContext = framework.getBundleContext();

        ServiceReference<?>[] serviceReferences = bundleContext.getServiceReferences((String) null, null);
        for (int i = 0; i < serviceReferences.length; i++) {
            ServiceReference<?> serviceReference = serviceReferences[i];
            System.out.println("serviceReference = " + serviceReference);
        }

        ServiceReference serviceReference = bundleContext.getServiceReference("" + OperatorRegistry.class.getName());
        System.out.println("serviceReference = " + serviceReference);
        if (serviceReference != null) {
            OperatorRegistry operatorRegistry2 = (OperatorRegistry) bundleContext.getService(serviceReference);

            System.out.println("operatorRegistry2 = " + operatorRegistry2);
            for (String opName : operatorRegistry2.getOperatorSpis().keySet()) {
                System.out.println("  opName: " + opName);
            }
        }
        launcher.stopAndWait();
    }
}
