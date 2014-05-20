package org.teo.ext2;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.teo.core.Operator;
import org.teo.core.OperatorRegistry;
import org.teo.core.OperatorSpi;
import org.teo.core.Product;

/**
 * @author Norman Fomferra
 */
public class Ext2Activator implements BundleActivator {

    private OperatorSpi opASpi;
    private OperatorSpi opBSpi;

    @Override
    public void start(final BundleContext context) throws Exception {
        opASpi = new OperatorSpi("Ext1.A") {
            @Override
            public Operator createOperator() {
                return new Operator() {
                    @Override
                    public Product process(Product source) {
                        return new Product(source.getName() + ".Ext1.A");
                    }
                };
            }
        };
        opBSpi = new OperatorSpi("Ext1.B") {
            @Override
            public Operator createOperator() {
                return new Operator() {
                    @Override
                    public Product process(Product source) {
                        return new Product(source.getName() + ".Ext1.B");
                    }
                };
            }
        };

        ServiceTracker<OperatorRegistry, OperatorRegistry> tracker = new ServiceTracker<>(context, OperatorRegistry.class, new ServiceTrackerCustomizer<OperatorRegistry, OperatorRegistry>() {
            @Override
            public OperatorRegistry addingService(ServiceReference<OperatorRegistry> reference) {
                OperatorRegistry service = context.getService(reference);
                service.add(opASpi);
                service.add(opBSpi);
                return service;
            }

            @Override
            public void removedService(ServiceReference<OperatorRegistry> reference, OperatorRegistry service) {
                service.remove(opASpi);
                service.remove(opBSpi);
            }

            @Override
            public void modifiedService(ServiceReference<OperatorRegistry> reference, OperatorRegistry service) {
            }
        });
    }

    @Override
    public void stop(BundleContext context) {
    }
}
