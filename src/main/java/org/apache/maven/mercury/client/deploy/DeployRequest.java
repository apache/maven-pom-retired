package org.apache.maven.mercury.client.deploy;

import java.util.Set;

import org.apache.maven.mercury.client.Binding;
import org.apache.maven.mercury.validate.Validator;


/**
 * RetrievalRequest
 * <p/>
 * A set of files to retrieve from remote locations.
 */
public interface DeployRequest
{
    public abstract Set<Binding> getBindings();

    public abstract boolean isFailFast();
    
    public Set<Validator> getValidators();
}
