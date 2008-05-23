package org.apache.maven.mercury.client.deploy;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.maven.mercury.client.BatchException;

public class DefaultDeployResponse implements DeployResponse
{
    private Set<BatchException> _exceptions = Collections.synchronizedSet( new HashSet<BatchException>() );

    public DefaultDeployResponse()
    {
    }

    public void add( BatchException e )
    {
        _exceptions.add( e );
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
