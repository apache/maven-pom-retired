package org.apache.maven.mercury.client.retrieve;

/**
 * Retriever
 * <p/>
 * Component to retrieve a set of remote files as an atomic operation.
 */
public interface Retriever
{
    /**
     * Retrieve a set of artifacts and wait until all retrieved successfully
     * or an error occurs.
     * <p/>
     * Note: whilst this method is synchronous for the caller, the implementation
     * will be asynchronous so many artifacts are fetched in parallel.
     *
     * @param request
     * @return
     */
    RetrievalResponse retrieve( RetrievalRequest request );


    /**
     * Retrieve a set of artifacts without waiting for the results.
     * When all results have been obtained (or an error occurs) the
     * RetrievalResponse will be called.
     *
     * @param request
     * @param callback
     */
    void retrieve( RetrievalRequest request, RetrievalCallback callback );
}
