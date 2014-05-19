package org.teo.obr;

import org.apache.felix.bundlerepository.RepositoryAdmin;
import org.osgi.service.deploymentadmin.DeploymentAdmin;

/**
 * Created by Norman on 15.05.2014.
 */
public interface ObrManager {
    DeploymentAdmin getDeploymentAdmin();

    RepositoryAdmin getRepositoryAdmin();

}
