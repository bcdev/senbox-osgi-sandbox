package org.teo.appadmin;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.application.ApplicationDescriptor;
import org.osgi.service.application.ApplicationHandle;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashSet;
import java.util.Set;

public class AppAdminActivator implements BundleActivator, ExecutableManager {
    public static final String APPLICATION_DESCRIPTOR = "org.osgi.vendor.application.ApplicationDescriptor";
    public static final String APPLICATION_HANDLE = "org.osgi.vendor.application.ApplicationHandle";

    BundleContext context;
    Set<ExecutableHandle> handles = new HashSet<>();

    public void registerExecutableHandle(ExecutableHandle handle) {
        handles.add(handle);
        handle.registration = context.registerService(ApplicationHandle.class.getName(), handle, handle.getServiceProperties());
    }

    public void registerExecutableDescriptor(ExecutableDescriptor descriptor) {
        descriptor.registration = context.registerService(ApplicationDescriptor.class.getName(), descriptor, descriptor.getServiceProperties());
    }

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        if (System.getProperty(APPLICATION_DESCRIPTOR) == null) {
            System.setProperty(APPLICATION_DESCRIPTOR, ApplicationDescriptorDelegate.class.getName());
        }
        if (System.getProperty(APPLICATION_HANDLE) == null) {
            System.setProperty(APPLICATION_HANDLE, ApplicationHandleDelegate.class.getName());
        }

        this.context = bundleContext;

        File dir = new File("c:/windows");
        String[] exeFileNames = dir.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".exe");
            }
        });
        if (exeFileNames != null) {
            for (String exeFileName : exeFileNames) {
                registerExecutableDescriptor(new ExecutableDescriptor(this, new File(dir, exeFileName)));
            }
        }
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {

        for (ExecutableHandle handle : handles) {
            try {
                handle.destroy();
            } catch (Exception e) {
                // Ok, since we are cleaning up ...
                e.printStackTrace();
            }
        }

        this.context = null;
    }
}