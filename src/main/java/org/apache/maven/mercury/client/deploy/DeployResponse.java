package org.apache.maven.mercury.client.deploy;

import java.util.Set;

import org.apache.maven.mercury.client.BatchException;

/**
 * DeployResponse
 * <p/>
 * A response to a request to upload a set of files to
 * remote location(s).
 */
public interface DeployResponse
{
    /**
     * The set will be empty if the operation completed successfully,
     * or will contain a single entry if the Request is failFast, otherwise
     * there will be one exception for every Binding in the Request.
     *
     * @return
     */
    public Set<BatchException> getExceptions();
}
