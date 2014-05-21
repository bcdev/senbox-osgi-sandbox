package org.teo.apiusage;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
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

        // Framework configuration for using exposed 'teo-core' API.
        //
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

        // Wait until the service is available
        OperatorRegistry frameworkOperatorRegistry = waitForService(framework);

        // Then assert that the host OperatorRegistry is the same as the framework OperatorRegistry instance.
        //dumpAvailableFrameworkServices(framework);
        dumpRegistry("From host app", OperatorRegistry.INSTANCE);
        dumpRegistry("From OSGi framework", frameworkOperatorRegistry);

        launcher.stopAndWait();
    }

    private static OperatorRegistry waitForService(Framework framework) throws InterruptedException {
        ServiceTracker<OperatorRegistry, OperatorRegistry> tracker = new ServiceTracker<>(
                framework.getBundleContext(), OperatorRegistry.class.getName(), null);
        tracker.open();

        OperatorRegistry operatorRegistry;
        while (true) {
            operatorRegistry = tracker.getService();
            if (operatorRegistry != null) {
                break;
            }
            System.out.println("Waiting another 100 ms for the service...");
            Thread.currentThread().sleep(100);
        }

        return operatorRegistry;
    }

    private static void dumpAvailableFrameworkServices(Framework framework) throws InvalidSyntaxException {
        ServiceReference<?>[] allServiceReferences = framework.getBundleContext().getServiceReferences((String) null, null);
        for (int i = 0; i < allServiceReferences.length; i++) {
            ServiceReference<?> serviceReference = allServiceReferences[i];
            System.out.println("allServiceReferences[" + i + "] = " + serviceReference);
        }
    }

    private static void dumpRegistry(String label, OperatorRegistry operatorRegistry) {
        System.out.println(label + ": operatorRegistry = " + operatorRegistry);
        for (String opName : operatorRegistry.getOperatorSpis().keySet()) {
            System.out.println("  opName: " + opName);
        }
    }
}
