package org.teo.appadmin;

import org.osgi.service.application.ApplicationHandle;

/**
 * Created by Norman on 27.03.2014.
 */
public class ApplicationHandleDelegate {
    private ApplicationHandle applicationHandle;
    private ApplicationDescriptorDelegate applicationDescriptorDelegate;

    /**
     * Called once by any {@link ApplicationHandle} subclass instance.
     *
     * @param applicationHandle
     * @param descriptor
     */
    public void setApplicationHandle(ApplicationHandle applicationHandle, Object descriptor) {
        this.applicationHandle = applicationHandle;
        System.out.println(getClass().getName() + ".setApplicationHandle(h = " + applicationHandle + ", descriptor = " + descriptor + ")");
        if (descriptor instanceof ApplicationDescriptorDelegate) {
            applicationDescriptorDelegate = (ApplicationDescriptorDelegate) descriptor;
        } else {
            throw new IllegalArgumentException("descriptor must be instance of " + ApplicationDescriptorDelegate.class);
        }
    }

    /**
     * The application instance's lifecycle state can be influenced by this
     * method. It lets the application instance perform operations to stop
     * the application safely, e.g. saving its state to a permanent storage.
     * <p/>
     * The method must check if the lifecycle transition is valid; a STOPPING
     * application cannot be stopped. If it is invalid then the method must
     * exit. Otherwise the lifecycle state of the application instance must be
     * set to STOPPING. Then the destroySpecific() method must be called to
     * perform any application model specific steps for safe stopping of the
     * represented application instance.
     * <p/>
     * At the end the <code>ApplicationHandle</code> must be unregistered.
     * This method should  free all the resources related to this
     * <code>ApplicationHandle</code>.
     * <p/>
     * When this method is completed the application instance has already made
     * its operations for safe stopping, the ApplicationHandle has been
     * unregistered and its related resources has been freed. Further calls on
     * this application should not be made because they may have unexpected
     * results.
     *
     * @throws SecurityException     if the caller doesn't have "lifecycle"
     *                               <code>ApplicationAdminPermission</code> for the corresponding application.
     * @throws IllegalStateException if the application handle is unregistered
     */
    public final void destroy() {
        System.out.println(getClass().getName() + ".destroy()");
    }
}
