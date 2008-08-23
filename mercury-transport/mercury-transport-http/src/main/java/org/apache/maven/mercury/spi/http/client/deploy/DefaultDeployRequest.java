package org.apache.maven.mercury.spi.http.client.deploy;

import java.util.HashSet;
import java.util.Set;

import org.apache.maven.mercury.spi.http.validate.Validator;
import org.apache.maven.mercury.transport.api.Binding;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class DefaultDeployRequest
implements DeployRequest
{
    private Set<Binding> _bindings = new HashSet<Binding>();
    private boolean _failFast;
    
    public Set<Validator> getValidators()
    {
        return null;
    }
    public void setBindings(Set<Binding> bindings)
    {
        _bindings=bindings;
    }
    public Set<Binding> getBindings()
    {
        return _bindings;
    }

    public boolean isFailFast()
    {
        return _failFast;
    }
    
    public void setFailFast (boolean f)
    {
        _failFast=f;
    }
}
