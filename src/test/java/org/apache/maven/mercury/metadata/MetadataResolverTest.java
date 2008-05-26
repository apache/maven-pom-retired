package org.apache.maven.mercury.metadata;

import java.io.File;
import java.util.List;

import org.apache.maven.mercury.repository.DefaultLocalRepository;
import org.apache.maven.mercury.repository.DefaultRemoteRepository;
import org.apache.maven.mercury.repository.DefaultRepositoryLayout;
import org.apache.maven.mercury.repository.LocalRepository;
import org.apache.maven.mercury.repository.RemoteRepository;
import org.apache.maven.mercury.repository.Repository;
import org.apache.maven.mercury.repository.RepositoryLayout;
import org.apache.maven.mercury.retrieve.DefaultArtifactRetriever;
import org.apache.maven.mercury.spi.http.server.SimpleTestServer;

public class MetadataResolverTest
{
    protected SimpleTestServer server;

    protected void setUp()
        throws Exception
    {
        server = new SimpleTestServer();
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
        
        LocalRepository localRepository = new DefaultLocalRepository( "local", layout, new File( "" ) );
        RemoteRepository remoteRepository = new DefaultRemoteRepository( "remote", layout, "");
        
        MetadataResolutionRequest request = new MetadataResolutionRequest()
            .setQuery( metadata )
            .setLocalRepository( localRepository )
            .addRemoteRepository( remoteRepository );
        
        MetadataResolutionResult result = metadataResolver.resolve( request );    
    }

    class SimpleSource
        implements MetadataSource
    {

        public MetadataResolution retrieve( ArtifactMetadata artifact, Repository localRepository, List<Repository> remoteRepositories )
            throws MetadataRetrievalException
        {
            // TODO Auto-generated method stub
            return null;
        }

    }
}
