package org.apache.maven.mercury.client.retrieve;

import java.util.Set;

import org.apache.maven.mercury.client.BatchException;


/**
 * RetrievalResponse
 * <p/>
 * Response from a request to download a set of files.
 */
public interface RetrievalResponse
{
    /**
     * The set will be empty if the operation completed successfully,
     * or will contain a single entry if the Request is failFast, otherwise
     * there will be one exception for every Binding in the Request.
     *
     * @return
     */
    Set<BatchException> getExceptions();
}
