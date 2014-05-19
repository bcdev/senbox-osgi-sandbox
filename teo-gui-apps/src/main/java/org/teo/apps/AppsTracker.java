package org.teo.apps;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.application.ApplicationDescriptor;
import org.osgi.util.tracker.ServiceTracker;

public class AppsTracker {
    final static String FILTER_SRC =
            String.format("(&(objectclass=%s)" +
                                  "(application.launchable=true)" +
                                  "(application.visible=true)" +
                                  "(application.locked=false))", ApplicationDescriptor.class.getName());

    final static String FILTER2_SRC =
            String.format("(objectclass=%s)", ApplicationDescriptor.class.getName());

    static ApplicationDescriptor[] EMPTY = new ApplicationDescriptor[0];

    ServiceTracker<ApplicationDescriptor, ApplicationDescriptor> tracker;

    public void init(BundleContext bundleContext) {
        Filter filter;
        try {
            filter = bundleContext.createFilter(FILTER2_SRC);
        } catch (InvalidSyntaxException e) {
            throw new IllegalStateException(e);
        }
        tracker = new ServiceTracker<>(bundleContext, filter, null);
        tracker.open();
    }

    public ApplicationDescriptor[] getLaunchableApps() {
        return tracker.getServices(EMPTY);
    }
}
