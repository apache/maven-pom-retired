package org.apache.maven.mercury.spi.http.server;

import org.mortbay.jetty.security.BasicAuthenticator;
import org.mortbay.jetty.security.Constraint;
import org.mortbay.jetty.security.ConstraintMapping;
import org.mortbay.jetty.security.HashUserRealm;
import org.mortbay.jetty.security.SecurityHandler;

public class AuthenticatingTestServer extends SimpleTestServer
{
    public AuthenticatingTestServer()
    throws Exception
    {
        super();
        
        HashUserRealm realm = new HashUserRealm();
        realm.put ("foo", "bar");
        realm.addUserToRole("foo", "foomeister");
        realm.setName("foorealm");
        
        SecurityHandler securityHandler = new SecurityHandler();
        securityHandler.setAuthenticator(new BasicAuthenticator());
        securityHandler.setUserRealm(realm);
        Constraint constraint = new Constraint();
        constraint.setAuthenticate(true);
        constraint.setRoles(new String[]{"foomeister"});
        ConstraintMapping cm = new ConstraintMapping();
        cm.setConstraint(constraint);
        cm.setPathSpec("/*");
        securityHandler.setConstraintMappings(new ConstraintMapping[]{cm});
        context.addHandler(securityHandler);
    }

}
