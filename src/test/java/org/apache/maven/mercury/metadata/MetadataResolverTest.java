package org.apache.maven.mercury.metadata;

import java.io.File;
import java.util.Set;

import org.apache.maven.mercury.Artifact;
import org.apache.maven.mercury.repository.DefaultLocalRepository;
import org.apache.maven.mercury.repository.DefaultRemoteRepository;
import org.apache.maven.mercury.repository.LocalRepository;
import org.apache.maven.mercury.repository.RemoteRepository;
import org.apache.maven.mercury.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.mercury.repository.layout.RepositoryLayout;
import org.apache.maven.mercury.retrieve.DefaultArtifactRetriever;
import org.apache.maven.mercury.spi.http.server.SimpleTestServer;

public class MetadataResolverTest
{
    protected File localRepositoryDirectory;
    protected String remoteRepository;
    protected SimpleTestServer server;
    protected File workDirectory;
    
    protected String localPathFragment;
    protected String remotePathFragment;
    protected String remoteRepositoryHostUrl;

    protected void setUp()
        throws Exception
    {
        if ( System.getProperty( "basedir" ) != null )
        {
            workDirectory = new File( System.getProperty( "basedir"), "target" );
        }
        else
        {
            workDirectory = new File( "", "target" );            
        }
        
        localRepositoryDirectory = new File( workDirectory, "repository" );
        localPathFragment = "/repo/";
        remotePathFragment = "/repo/";
        remoteRepositoryHostUrl = "http://localhost" + remotePathFragment;
        server = new SimpleTestServer( localPathFragment, remotePathFragment );
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
        RepositoryLayout layout = new DefaultRepositoryLayout();

        MetadataResolver metadataResolver = new DefaultMetadataResolver( new DefaultArtifactRetriever(), source );
        ArtifactMetadata metadata = new ArtifactMetadata( "a", "a", "1.0" );

        LocalRepository localRepository = new DefaultLocalRepository( "local", layout, localRepositoryDirectory );
        RemoteRepository remoteRepository = new DefaultRemoteRepository( "remote", layout, remoteRepositoryHostUrl );

        MetadataResolutionRequest request = new MetadataResolutionRequest().setQuery( metadata ).setLocalRepository( localRepository ).addRemoteRepository( remoteRepository );

        // Do we do this in memory, in which case the layout?
        
        MetadataResolutionResult result = metadataResolver.resolve( request );
    }

    class SimpleSource
        implements MetadataSource
    {
        public MetadataResolution retrieve( ArtifactMetadata artifact, LocalRepository localRepository, Set<RemoteRepository> remoteRepositories )
            throws MetadataRetrievalException
        {
            // TODO Auto-generated method stub
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
