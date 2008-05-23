package org.apache.maven.mercury.client.deploy;

/**
 * DeployCallback
 * <p/>
 * Classes that implement this method will be notified when a given job has
 * been completed and validated.
 */
public interface DeployCallback
{
    /**
     * Callback for asynchronous version of Retriever.retrieve.
     *
     * @param response empty if all artifacts retrieved ok, list of exceptions otherwise
     */
    void onComplete( DeployResponse response );
}
