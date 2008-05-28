package org.apache.maven.mercury.metadata;

import java.io.File;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.maven.mercury.Artifact;
import org.apache.maven.mercury.repository.DefaultLocalRepository;
import org.apache.maven.mercury.repository.DefaultRemoteRepository;
import org.apache.maven.mercury.repository.LocalRepository;
import org.apache.maven.mercury.repository.RemoteRepository;
import org.apache.maven.mercury.repository.layout.RepositoryLayout;
import org.apache.maven.mercury.retrieve.DefaultArtifactRetriever;
import org.apache.maven.mercury.spi.http.server.HttpTestServer;

public class MetadataResolverTest
    extends TestCase
{
    protected File localRepositoryDirectory;
    protected String remoteRepository;
    protected HttpTestServer server;
    protected File workDirectory;
    protected String remotePathFragment;
    protected String remoteRepositoryHostUrl;
    protected File webserverResourceDirectory;
    protected File basedir;
    
    protected void setUp()
        throws Exception
    {
        if ( System.getProperty( "basedir" ) != null )
        {
            basedir = new File( System.getProperty( "basedir" ) );

        }
        else
        {
            basedir = new File( "" );
        }

        workDirectory = new File( System.getProperty( "basedir" ), "target" );
        webserverResourceDirectory = new File( basedir, "src/test/resources/repo" );        
        localRepositoryDirectory = new File( workDirectory, "repository" );
        remotePathFragment = "/repo";
        server = new HttpTestServer( webserverResourceDirectory, remotePathFragment );
        server.start();      
        remoteRepositoryHostUrl = "http://localhost:" + server.getPort() + remotePathFragment;
    }

    protected void tearDown()
        throws Exception
    {
       server.stop();
    }

    public void testMetadataResolver()
        throws Exception
    {
        // Really the source encapsulates the layout, and any other specical attributes associated with an artifact/model
        // simple properties
        // maven
        // ivy
        // obr
        
        MetadataSource source = new SimpleSource();
        RepositoryLayout layout = new SimpleLayout();

        MetadataResolver metadataResolver = new DefaultMetadataResolver( new DefaultArtifactRetriever(), source );
        ArtifactMetadata metadata = new ArtifactMetadata( "a", "a", "1.0", "foo" );

        LocalRepository localRepository = new DefaultLocalRepository( "local", layout, localRepositoryDirectory );
        RemoteRepository remoteRepository = new DefaultRemoteRepository( "remote", layout, remoteRepositoryHostUrl );

        MetadataResolutionRequest request = new MetadataResolutionRequest().setQuery( metadata ).setLocalRepository( localRepository ).addRemoteRepository( remoteRepository );

        // Resolving transitively or not
        MetadataResolutionResult result = metadataResolver.resolve( request );
        
        assertNotNull( result.getTree() );
        
        System.out.println(result.getTree());
    }

    class SimpleSource
        implements MetadataSource
    {
        public MetadataResolution retrieve( ArtifactMetadata artifact, LocalRepository localRepository, Set<RemoteRepository> remoteRepositories )
            throws MetadataRetrievalException
        {
            //TODO: This assumes that we have already pulled it down

            return null;
        }

    }

    class SimpleLayout
        implements RepositoryLayout
    {
        public String pathOf( Artifact artifact )
        {
            return artifact.getArtifactId() + "-" + artifact.getVersion() + ".txt";
        }
    }
}
