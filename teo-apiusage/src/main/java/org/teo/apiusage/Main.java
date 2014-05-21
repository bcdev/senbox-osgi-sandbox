package org.teo.apiusage;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;
import org.osgi.util.tracker.ServiceTracker;
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
        //System.setProperty(Constants.FRAMEWORK_BEGINNING_STARTLEVEL, "3");
        // see section 4.2.2 Launching Properties ot the core spec
        //System.setProperty("org.osgi.framework.system.packages.extra", "org.teo.core");
        //System.setProperty("org.osgi.framework.system.packages.extra", "org.teo.core");
        //System.setProperty("org.osgi.framework.system.packages.extra", "org.teo.core; version=0.1.0.SNAPSHOT");
        System.setProperty("org.osgi.framework.bundle.parent", "app");
        //System.setProperty("org.osgi.framework.bundle.parent", "framework");
        //System.setProperty("org.osgi.framework.bootdelegation", "org.teo.core,org.teo.core.*");
        //System.setProperty("org.osgi.framework.bootdelegation", "org.teo.core");
        System.setProperty("org.osgi.framework.bootdelegation", "org.teo.*,org.teo.core.*");
        Launcher launcher = Launcher.start();

        Framework framework = launcher.getFramework();
        while (framework.getState() != Bundle.ACTIVE) {
            Thread.currentThread().sleep(100);
        }

        BundleContext bundleContext = framework.getBundleContext();

        ServiceReference<?>[] allServiceReferences = bundleContext.getServiceReferences((String) null, null);
        for (int i = 0; i < allServiceReferences.length; i++) {
            ServiceReference<?> serviceReference = allServiceReferences[i];
            System.out.println("allServiceReferences[" + i + "] = " + serviceReference);
        }

        ServiceTracker<OperatorRegistry, OperatorRegistry> tracker = new ServiceTracker<>(
                framework.getBundleContext(), OperatorRegistry.class.getName(), null);
        tracker.open(true);

        OperatorRegistry operatorRegistry2 = null;
        while (true) {
            operatorRegistry2 = tracker.getService();
            System.out.println("operatorRegistry2 = " + operatorRegistry2);
            if (operatorRegistry2 != null) {
                break;
            }
            Thread.currentThread().sleep(100);
        }


        ServiceReference serviceReference = bundleContext.getServiceReference(OperatorRegistry.class.getName());
        System.out.println("serviceReference = " + serviceReference);
        if (serviceReference != null) {
            OperatorRegistry operatorRegistry3 = (OperatorRegistry) bundleContext.getService(serviceReference);

            System.out.println("operatorRegistry3 = " + operatorRegistry3);
            for (String opName : operatorRegistry3.getOperatorSpis().keySet()) {
                System.out.println("  opName: " + opName);
            }
        }
        launcher.stopAndWait();
    }
}
