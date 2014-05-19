package org.teo.appadmin;

import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.application.ApplicationDescriptor;
import org.osgi.service.application.ApplicationHandle;

import java.io.File;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

// Sample from OSGi Compendium M5
public class ExecutableDescriptor extends ApplicationDescriptor {
    ServiceRegistration registration;
    File executable;
    ExecutableManager model;
    boolean locked;
    //static URL genericIcon = SimpleDescriptor.class.getResource("icon.png");

    public ExecutableDescriptor(ExecutableManager model, File executable) {
        super(executable.getName());
        this.model = model;
        this.executable = executable;
    }

    @Override
    public Map getPropertiesSpecific(String locale) {
        Map<String, Object> map = new Hashtable<>();
        //map.put(APPLICATION_ICON, genericIcon);
        map.put(APPLICATION_NAME, executable.getName());
        map.put(APPLICATION_VISIBLE, Boolean.TRUE);
        map.put(APPLICATION_LAUNCHABLE, Boolean.TRUE);
        return map;
    }

    @Override
    protected ApplicationHandle launchSpecific(final Map args) throws Exception {
        final ExecutableDescriptor descriptor = this;
        return (ApplicationHandle) AccessController.doPrivileged(new PrivilegedExceptionAction() {
            public Object run() throws Exception {
                ExecutableHandle handle = new ExecutableHandle(descriptor, args);
                model.registerExecutableHandle(handle);
                return handle;
            }
        });
    }

    Dictionary getServiceProperties() {
        Hashtable<String, Object> p = new Hashtable<>();
        p.put(APPLICATION_LAUNCHABLE, Boolean.TRUE);
        p.put(APPLICATION_LOCKED, locked);
        p.put(Constants.SERVICE_PID, getApplicationId());
        return p;
    }

    @Override
    protected void lockSpecific() {
        locked = true;
    }

    @Override
    protected void unlockSpecific() {
        locked = false;
    }

    @Override
    public boolean matchDNChain(String arg) {
        return false;
    }

    @Override
    protected boolean isLaunchableSpecific() {
        return true;
    }
}