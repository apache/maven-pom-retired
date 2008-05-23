package org.apache.maven.mercury.client.retrieve;

import java.util.Set;

import org.apache.maven.mercury.client.Binding;
import org.apache.maven.mercury.validate.Validator;


/**
 * RetrievalRequest
 * <p/>
 * A set of files to retrieve from remote locations
 * and a set of validators to apply to them.
 */
public interface RetrievalRequest
{
    public abstract Set<Binding> getBindings();

    public abstract boolean isFailFast();
    
    public Set<Validator> getValidators();
}
