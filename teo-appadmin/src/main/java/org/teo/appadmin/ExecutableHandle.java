package org.teo.appadmin;

import org.osgi.framework.ServiceRegistration;
import org.osgi.service.application.ApplicationHandle;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

// Sample from OSGi Compendium M5
public class ExecutableHandle extends ApplicationHandle implements Runnable {

    static int INSTANCE = 0;

    ServiceRegistration registration;
    Process process;
    String state;
    Thread thread;

    public ExecutableHandle(ExecutableDescriptor descriptor, Map arguments) throws IOException {
        super(String.format("%s:%d", descriptor.getApplicationId(), INSTANCE++), descriptor);
        state = RUNNING;
        String path = descriptor.executable.getAbsolutePath();
        process = Runtime.getRuntime().exec(path);
        thread = new Thread(this, getInstanceId());
        thread.start();
    }

    @Override
    public String getState() {
        return state;
    }

    @Override
    protected void destroySpecific() {
        state = STOPPING;
        registration.setProperties(getServiceProperties());
        registration.unregister();
        thread.interrupt();
    }

    // Wait until process finishes or when interrupted
    @Override
    public void run() {
        try {
            process.waitFor();
            destroy();
        } catch (InterruptedException ie) {
            process.destroy();
            try {
                process.waitFor();
            } catch (InterruptedException ignored) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    Dictionary getServiceProperties() {
        Hashtable<String, Object> p = new Hashtable<>();
        p.put(APPLICATION_PID, getInstanceId());
        p.put(APPLICATION_STATE, state);
        p.put(APPLICATION_DESCRIPTOR, getApplicationDescriptor().getApplicationId());
        return p;
    }
}