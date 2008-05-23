package org.apache.maven.mercury.client;

import java.util.HashSet;
import java.util.Set;

import org.apache.maven.mercury.client.Binding;
import org.apache.maven.mercury.client.retrieve.RetrievalRequest;
import org.apache.maven.mercury.validate.Validator;

public class RetrievalRequestImpl implements RetrievalRequest
{
    private boolean _isFailFast;
    private Set<Binding> _bindings;
    private Set<Validator> _validators;
    
    public void setFailFast(boolean failFast)
    {
        _isFailFast = failFast;
    }

    public boolean isFailFast()
    {
        return _isFailFast;
    }
    
    public void setBindings(Set<Binding> bindings)
    {
        _bindings = new HashSet<Binding>(bindings);
    }
    
    public Set<Binding> getBindings()
    {
        return _bindings;
    }

    public void setValidators(Set<Validator> validators)
    {
        _validators = new HashSet<Validator>(validators);
    }
    
    public Set<Validator> getValidators()
    {
        return _validators;
    }
    
    public String toString()
    {
        return _bindings.toString()+"|"+_isFailFast;
    }

}
