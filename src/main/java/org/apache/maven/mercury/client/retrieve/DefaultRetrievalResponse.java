package org.apache.maven.mercury.client.retrieve;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.maven.mercury.client.BatchException;

public class DefaultRetrievalResponse implements RetrievalResponse
{
    private Set<BatchException> _exceptions = Collections.synchronizedSet( new HashSet<BatchException>() );

    public DefaultRetrievalResponse()
    {
    }

    protected void add( BatchException exception )
    {
        _exceptions.add( exception );
    }

    public Set<BatchException> getExceptions()
    {
        return _exceptions;
    }

    public String toString()
    {
        return _exceptions.toString();
    }

}
