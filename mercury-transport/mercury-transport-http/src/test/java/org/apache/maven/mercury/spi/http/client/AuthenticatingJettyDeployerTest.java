package org.apache.maven.mercury.spi.http.client;

import java.net.URL;
import java.util.HashSet;

import org.apache.maven.mercury.crypto.api.StreamVerifierFactory;
import org.apache.maven.mercury.spi.http.client.deploy.DefaultDeployer;
import org.apache.maven.mercury.spi.http.server.AuthenticatingPutServer;
import org.apache.maven.mercury.transport.api.Credentials;

public class AuthenticatingJettyDeployerTest extends JettyDeployerTest
{

    public AuthenticatingJettyDeployerTest () throws Exception
    {
        super();
    }

    protected void setUp() throws Exception
    {        
        setUpFiles();
        _deployer = new DefaultDeployer();
        _putServer = new AuthenticatingPutServer();
        _putServer.start();
        _port = String.valueOf(_putServer.getPort());
        setUpServerType();
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }
    
    protected void setUpServerType () throws Exception
    {
        HashSet<org.apache.maven.mercury.transport.api.Server> remoteServerTypes = new HashSet<org.apache.maven.mercury.transport.api.Server>();
        remoteServerType = new org.apache.maven.mercury.transport.api.Server( "test", new URL(_HOST_FRAGMENT+_port), false, false, new Credentials("foo", "bar"));
        factories = new HashSet<StreamVerifierFactory>();       
        remoteServerTypes.add(remoteServerType);
        _deployer.setServers(remoteServerTypes);
    }
}
