package org.teo.obr;

import org.apache.felix.bundlerepository.RepositoryAdmin;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.deploymentadmin.DeploymentAdmin;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.teo.gui.Gui;
import org.teo.gui.command.CommandGroup;

public class ObrActivator implements BundleActivator, ObrManager {
    private ServiceTracker<DeploymentAdmin, DeploymentAdmin> deploymentAdminTracker;
    private ServiceTracker<RepositoryAdmin, RepositoryAdmin> repositoryAdminTracker;

    DeploymentAdmin deploymentAdmin;
    RepositoryAdmin repositoryAdmin;

    ObrCommand obrCommand;
    private DpCommand dpCommand;

    @Override
    public DeploymentAdmin getDeploymentAdmin() {
        return deploymentAdmin;
    }

    public void setDeploymentAdmin(DeploymentAdmin deploymentAdmin) {
        this.deploymentAdmin = deploymentAdmin;
        dpCommand.updateState();
        obrCommand.updateState();
    }

    @Override
    public RepositoryAdmin getRepositoryAdmin() {
        return repositoryAdmin;
    }

    public void setRepositoryAdmin(RepositoryAdmin repositoryAdmin) {
        this.repositoryAdmin = repositoryAdmin;
        dpCommand.updateState();
        obrCommand.updateState();
    }

    @Override
    public void start(BundleContext bundleContext) {
        ServiceReference serviceReference = bundleContext.getServiceReference(Gui.class.getName());
        if (serviceReference != null) {
            Gui gui = (Gui) bundleContext.getService(serviceReference);
            CommandGroup helpGroup = gui.getCommandGroup("help");
            obrCommand = new ObrCommand(gui, this);
            dpCommand = new DpCommand(gui, this);
            helpGroup.addCommand(obrCommand);
            helpGroup.addCommand(dpCommand);
            trackRepositoryAdmin(bundleContext);
            trackDeploymentAdmin(bundleContext);
        } else {
            System.out.println(getClass().getName() + ": missing service " + Gui.class.getName());
        }
    }

    @Override
    public void stop(BundleContext bundleContext) {
        if (deploymentAdminTracker != null) {
            deploymentAdminTracker.close();
        }
        if (repositoryAdminTracker != null) {
            repositoryAdminTracker.close();
        }
    }


    private void trackRepositoryAdmin(final BundleContext bundleContext) {
        repositoryAdminTracker = new ServiceTracker<>(bundleContext, RepositoryAdmin.class, new ServiceTrackerCustomizer<RepositoryAdmin, RepositoryAdmin>() {
            @Override
            public RepositoryAdmin addingService(ServiceReference<RepositoryAdmin> reference) {
                RepositoryAdmin service = bundleContext.getService(reference);
                setRepositoryAdmin(service);
                return service;
            }

            @Override
            public void modifiedService(ServiceReference<RepositoryAdmin> reference, RepositoryAdmin service) {
            }

            @Override
            public void removedService(ServiceReference<RepositoryAdmin> reference, RepositoryAdmin service) {
                setRepositoryAdmin(null);
            }
        });
        repositoryAdminTracker.open();
        setRepositoryAdmin(repositoryAdminTracker.getService());
    }

    private void trackDeploymentAdmin(final BundleContext bundleContext) {
        deploymentAdminTracker = new ServiceTracker<>(bundleContext, DeploymentAdmin.class, new ServiceTrackerCustomizer<DeploymentAdmin, DeploymentAdmin>() {
            @Override
            public DeploymentAdmin addingService(ServiceReference<DeploymentAdmin> reference) {
                DeploymentAdmin deploymentAdmin = bundleContext.getService(reference);
                setDeploymentAdmin(deploymentAdmin);
                return deploymentAdmin;
            }

            @Override
            public void modifiedService(ServiceReference<DeploymentAdmin> reference, DeploymentAdmin service) {
            }

            @Override
            public void removedService(ServiceReference<DeploymentAdmin> reference, DeploymentAdmin service) {
                setDeploymentAdmin(null);
            }
        });
        deploymentAdminTracker.open();
        setDeploymentAdmin(deploymentAdminTracker.getService());
    }


}