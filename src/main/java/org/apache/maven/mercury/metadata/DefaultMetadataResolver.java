package org.apache.maven.mercury.metadata;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.maven.mercury.Artifact;
import org.apache.maven.mercury.DefaultArtifact;
import org.apache.maven.mercury.repository.LocalRepository;
import org.apache.maven.mercury.repository.RemoteRepository;
import org.apache.maven.mercury.repository.Repository;
import org.apache.maven.mercury.retrieve.ArtifactRetriever;
import org.apache.maven.mercury.retrieve.ResolutionRequest;
import org.apache.maven.mercury.retrieve.ResolutionResult;

/*
 * default implementation of the metadata resolver
 * 
 * @author Oleg Gusakov
 * @author Jason van Zyl
 * 
 * @plexus.component
 */
public class DefaultMetadataResolver
    implements MetadataResolver
{
    //------------------------------------------------------------------------

    /** @plexus.requirement */
    ArtifactRetriever retriever;

    /** @plexus.requirement */
    MetadataSource metadataSource;

    public DefaultMetadataResolver( ArtifactRetriever retriever, MetadataSource metadataSource )
    {
        this.retriever = retriever;
        this.metadataSource = metadataSource;
    }
    
    //------------------------------------------------------------------------
    public MetadataResolutionResult resolve( MetadataResolutionRequest request )
        throws MetadataResolutionException
    {
        MetadataResolutionResult result = new MetadataResolutionResult();

        // We need to make the root and send it into the resolution.
        
        MetadataTreeNode tree = resolveMetadataTree( request.getQuery(), null, request.getLocalRepository(), request.getRemoteRepositories() );

        result.setTree( tree );

        return result;
    }

    //------------------------------------------------------------------------
    private MetadataTreeNode resolveMetadataTree( ArtifactMetadata query, MetadataTreeNode parent, LocalRepository localRepository, Set<RemoteRepository> remoteRepositories )
        throws MetadataResolutionException
    {
        try
        {
            Artifact pomArtifact = new DefaultArtifact( query.getGroupId(), query.getArtifactId(), query.getVersion(), query.getType(), null, false, query.getScope(), null );

            ResolutionRequest request = new ResolutionRequest().setArtifact( pomArtifact ).setLocalRepository( localRepository ).setRemoteRepostories( remoteRepositories );

            ResolutionResult result = retriever.retrieve( request );

            // We need a mode that will find all the errors so that we can see where metadata cannot be retrieved.
            
            if ( result.hasExceptions() )
            {
                pomArtifact.setResolved( false );
            }

            if ( pomArtifact.isResolved() )
            {
                MetadataResolution metadataResolution = metadataSource.retrieve( query, localRepository, remoteRepositories );
                ArtifactMetadata found = metadataResolution.getArtifactMetadata();

                if ( pomArtifact.getFile() != null && pomArtifact.getFile().toURI() != null )
                {
                    found.setArtifactUri( pomArtifact.getFile().toURI().toString() );
                }

                MetadataTreeNode node = new MetadataTreeNode( found, parent, query, true, found.getScopeAsEnum() );
                Collection<ArtifactMetadata> dependencies = metadataResolution.getArtifactMetadata().getDependencies();

                if ( dependencies != null && dependencies.size() > 0 )
                {
                    for ( ArtifactMetadata a : dependencies )
                    {
                        MetadataTreeNode kidNode = resolveMetadataTree( a, node, localRepository, remoteRepositories );
                        node.addChild( kidNode );
                    }
                }
                return node;
            }
            else
            {
                return new MetadataTreeNode( new ArtifactMetadata(pomArtifact), parent, query, false, query.getArtifactScope() );
            }
        }
        catch ( Exception anyEx )
        {
            throw new MetadataResolutionException( anyEx );
        }
    }
}
