package org.apache.maven.mercury.client.deploy;

/**
 * Deployer
 * <p/>
 * Deploy a set of files to remote locations as an atomic operation.
 */
public interface Deployer
{
    /** Deploy a set of files and return when all done. */
    DeployResponse deploy( DeployRequest request );


    /**
     * Deploy a set of files and return immediately without waiting.
     * The callback will be called when files are ready or an error
     * has occurred.
     */
    void deploy( DeployRequest request, DeployCallback callback );
}
