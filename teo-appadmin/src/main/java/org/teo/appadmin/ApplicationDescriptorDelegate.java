package org.teo.appadmin;

import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.application.ApplicationDescriptor;
import org.osgi.service.application.ApplicationException;
import org.osgi.service.application.ScheduledApplication;

import java.util.Map;

/**
 * An OSGi service that represents an installed application and stores
 * information about it. The application descriptor can be used for instance
 * creation.
 */
public class ApplicationDescriptorDelegate {

    private ApplicationDescriptor applicationDescriptor;
    private String applicationId;

    /**
     * Called once by the {@link ApplicationDescriptor} after it created an instance of this class used as its delegate.
     *
     * @param applicationDescriptor
     * @param applicationId         The identifier of the application. Its value is also available
     *                              as the <code>service.pid</code> service property of this
     *                              <code>ApplicationDescriptor</code> service. This parameter must not
     *                              be <code>null</code>.
     */
    public void setApplicationDescriptor(ApplicationDescriptor applicationDescriptor, String applicationId) {
        this.applicationDescriptor = applicationDescriptor;
        this.applicationId = applicationId;
        System.out.println(getClass().getName() + ".setApplicationDescriptor(d = " + applicationDescriptor + ", applicationId = '" + applicationId + "')");
    }

    /**
     * Launches a new instance of an application. The <code>args</code> parameter specifies
     * the startup parameters for the instance to be launched, it may be null.
     * <p/>
     * The following steps are made:
     * <UL>
     * <LI>Check for the appropriate permission.
     * <LI>Check the locking state of the application. If locked then throw
     * an {@link org.osgi.service.application.ApplicationException} with the reason code
     * {@link org.osgi.service.application.ApplicationException#APPLICATION_LOCKED}.
     * <LI>Calls the <code>launchSpecific()</code> method to create and start an application
     * instance.
     * <LI>Returns the <code>ApplicationHandle</code> returned by the
     * launchSpecific()
     * </UL>
     * The caller has to have ApplicationAdminPermission(applicationPID,
     * "launch") in order to be able to perform this operation.
     * <p/>
     * The <code>Map</code> argument of the launch method contains startup
     * arguments for the
     * application. The keys used in the Map must be non-null, non-empty <code>String<code>
     * objects. They can be standard or application
     * specific. OSGi defines the <code>org.osgi.triggeringevent</code>
     * key to be used to
     * pass the triggering event to a scheduled application, however
     * in the future it is possible that other well-known keys will be defined.
     * To avoid unwanted clashes of keys, the following rules should be applied:
     * <ul>
     * <li>The keys starting with the dash (-) character are application
     * specific, no well-known meaning should be associated with them.</li>
     * <li>Well-known keys should follow the reverse domain name based naming.
     * In particular, the keys standardized in OSGi should start with
     * <code>org.osgi.</code>.</li>
     * </ul>
     * <p/>
     * The method is synchronous, it return only when the application instance was
     * successfully started or the attempt to start it failed.
     * <p/>
     * This method never returns <code>null</code>. If launching an application fails,
     * the appropriate exception is thrown.
     *
     * @param arguments Arguments for the newly launched application, may be null
     * @throws SecurityException                                 if the caller doesn't have "lifecycle"
     *                                                           ApplicationAdminPermission for the application.
     * @throws org.osgi.service.application.ApplicationException if starting the application failed
     * @throws IllegalStateException                             if the application descriptor is unregistered
     * @throws IllegalArgumentException                          if the specified <code>Map</code> contains invalid keys
     *                                                           (null objects, empty <code>String</code> or a key that is not
     *                                                           <code>String</code>)
     */
    public void launch(Map arguments) throws ApplicationException {
        System.out.println(getClass().getName() + ".launch(arguments = '" + arguments + "')");
    }


    /**
     * Schedules the application at a specified event. Schedule information
     * should not get lost even if the framework or the device restarts so it
     * should be stored in a persistent storage. The method registers a
     * {@link org.osgi.service.application.ScheduledApplication} service in Service Registry, representing
     * the created schedule.
     * <p/>
     * The <code>Map</code> argument of the  method contains startup
     * arguments for the application. The keys used in the Map must be non-null,
     * non-empty <code>String<code> objects. The argument values must be
     * of primitive types, wrapper classes of primitive types, <code>String</code>
     * or arrays or collections of these.
     * <p/>
     * The created schedules have a unique identifier within the scope of this
     * <code>ApplicationDescriptor</code>. This identifier can be specified
     * in the <code>scheduleId</code> argument. If this argument is <code>null</code>,
     * the identifier is automatically generated.
     *
     * @param scheduleId  the identifier of the created schedule. It can be <code>null</code>,
     *                    in this case the identifier is automatically generated.
     * @param arguments   the startup arguments for the scheduled application, may be
     *                    null
     * @param topic       specifies the topic of the triggering event, it may contain a
     *                    trailing asterisk as wildcard, the empty string is treated as
     *                    "*", must not be null
     * @param eventFilter specifies and LDAP filter to filter on the properties of the
     *                    triggering event, may be null
     * @param recurring   if the recurring parameter is false then the application will
     *                    be launched only once, when the event firstly occurs. If the
     *                    parameter is true then scheduling will take place for every
     *                    event occurrence; i.e. it is a recurring schedule
     * @return the registered scheduled application service
     * @throws NullPointerException                      if the topic is <code>null</code>
     * @throws org.osgi.framework.InvalidSyntaxException if the specified <code>eventFilter</code> is not syntactically correct
     * @throws ApplicationException                      if the schedule couldn't be created. The possible error
     *                                                   codes are
     *                                                   <ul>
     *                                                   <li> {@link ApplicationException#APPLICATION_DUPLICATE_SCHEDULE_ID}
     *                                                   if the specified <code>scheduleId</code> is already used
     *                                                   for this <code>ApplicationDescriptor</code>
     *                                                   <li> {@link ApplicationException#APPLICATION_SCHEDULING_FAILED}
     *                                                   if the scheduling failed due to some internal reason
     *                                                   (e.g. persistent storage error).
     *                                                   <li> {@link ApplicationException#APPLICATION_INVALID_STARTUP_ARGUMENT}
     *                                                   if the specified startup argument doesn't satisfy the
     *                                                   type or value constraints of startup arguments.
     *                                                   </ul>
     * @throws SecurityException                         if the caller doesn't have "schedule"
     *                                                   ApplicationAdminPermission for the application.
     * @throws IllegalStateException                     if the application descriptor is unregistered
     * @throws IllegalArgumentException                  if the specified <code>Map</code> contains invalid keys
     *                                                   (null objects, empty <code>String</code> or a key that is not
     *                                                   <code>String</code>)
     */
    public final ScheduledApplication schedule(String scheduleId, Map arguments, String topic,
                                               String eventFilter, boolean recurring) throws InvalidSyntaxException, ApplicationException {
        System.out.println(getClass().getName() + ".schedule(scheduleId = '" + scheduleId + "')");
        return null;
    }

    public boolean isLocked() {
        System.out.println(getClass().getName() + ".isLocked()");
        return false;
    }


    /**
     * Sets the lock state of the application. If an application is locked then
     * launching a new instance is not possible. It does not affect the already
     * launched instances.
     *
     * @throws SecurityException     if the caller doesn't have "lock" ApplicationAdminPermission
     *                               for the application.
     * @throws IllegalStateException if the application descriptor is unregistered
     */
    public void lock() {
        System.out.println(getClass().getName() + ".lock()");
    }


    /**
     * Unsets the lock state of the application.
     *
     * @throws SecurityException     if the caller doesn't have "lock" ApplicationAdminPermission
     *                               for the application.
     * @throws IllegalStateException if the application descriptor is unregistered
     */
    public void unlock() {
        System.out.println(getClass().getName() + ".unlock()");
    }
}
