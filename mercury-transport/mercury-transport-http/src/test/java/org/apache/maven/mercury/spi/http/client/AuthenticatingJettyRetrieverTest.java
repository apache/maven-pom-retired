package org.apache.maven.mercury.spi.http.client;

import java.net.URL;
import java.util.HashSet;

import org.apache.maven.mercury.crypto.api.StreamVerifierFactory;
import org.apache.maven.mercury.spi.http.client.retrieve.DefaultRetriever;
import org.apache.maven.mercury.spi.http.server.AuthenticatingTestServer;
import org.apache.maven.mercury.transport.api.Credentials;
import org.apache.maven.mercury.transport.api.Server;

public class AuthenticatingJettyRetrieverTest extends JettyRetrieverTest
{    
    public void setUp ()
    throws Exception
    {
        server = new AuthenticatingTestServer();
        server.start();
        _port=String.valueOf(server.getPort()); 

        HashSet<Server> remoteServerTypes = new HashSet<Server>();
        remoteServerType = new Server( "test", new URL(__HOST_FRAGMENT+_port), false, false, new Credentials("foo", "bar"));        
        factories = new HashSet<StreamVerifierFactory>();

        remoteServerTypes.add(remoteServerType);

        retriever = new DefaultRetriever();
        retriever.setServers(remoteServerTypes);
    }


    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

}
