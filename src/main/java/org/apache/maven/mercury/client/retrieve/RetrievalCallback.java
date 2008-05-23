package org.apache.maven.mercury.client.retrieve;

/**
 * Classes that implement this method will be notified when a given job has
 * been completed and validated.
 */
public interface RetrievalCallback
{
    /**
     * Callback for asynchronous version of Retriever.retrieve.
     *
     * @param response empty if all artifacts retrieved ok, list of exceptions otherwise
     */
    public abstract void onComplete( RetrievalResponse response );
}
