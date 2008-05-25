package org.apache.maven.mercury.metadata;

import java.util.Collection;
import java.util.List;

import org.apache.maven.mercury.Artifact;
import org.apache.maven.mercury.ArtifactRepository;
import org.apache.maven.mercury.DefaultArtifact;
import org.apache.maven.mercury.retrieve.ArtifactRetriever;
import org.apache.maven.mercury.retrieve.ResolutionRequest;
import org.apache.maven.mercury.retrieve.ResolutionResult;

/*
 * default implementation of the metadata resolver
 * 
 * @author Oleg Gusakov
 * 
 * @plexus.component
 */
public class DefaultMetadataResolver
    implements MetadataResolver
{
    //------------------------------------------------------------------------

    /** @plexus.requirement */
    ArtifactRetriever artifactResolver;

    /** @plexus.requirement */
    MetadataSource metadataSource;

    //------------------------------------------------------------------------
    public MetadataResolutionResult resolveMetadata( MetadataResolutionRequest request )
        throws MetadataResolutionException
    {
        MetadataResolutionResult result = new MetadataResolutionResult();

        MetadataTreeNode tree = resolveMetadataTree( request.getQuery(), null, request.getLocalRepository(), request.getRemoteRepositories() );

        result.setTree( tree );

        return result;
    }

    //------------------------------------------------------------------------
    private MetadataTreeNode resolveMetadataTree( ArtifactMetadata query, MetadataTreeNode parent, ArtifactRepository localRepository, List<ArtifactRepository> remoteRepositories )
        throws MetadataResolutionException
    {
        try
        {
            Artifact pomArtifact = new DefaultArtifact( query.getGroupId(), query.getArtifactId(), query.getVersion(), query.getType(), null, false, query.getScope(), null );

            ResolutionRequest request = new ResolutionRequest().setArtifact( pomArtifact ).setLocalRepository( localRepository ).setRemoteRepostories( remoteRepositories );

            ResolutionResult result = artifactResolver.retrieve( request );

            // Here we just need to deal with basic retrieval problems.
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

                MetadataTreeNode node = new MetadataTreeNode( found, parent, true, found.getScopeAsEnum() );
                Collection<ArtifactMetadata> dependencies = metadataResolution.getArtifactMetadata().getDependencies();

                if ( dependencies != null && dependencies.size() > 0 )
                {
                    int numberOfChildren = dependencies.size();
                    node.setNChildren( numberOfChildren );
                    int kidNo = 0;
                    for ( ArtifactMetadata a : dependencies )
                    {
                        MetadataTreeNode kidNode = resolveMetadataTree( a, node, localRepository, remoteRepositories );
                        node.addChild( kidNo++, kidNode );
                    }
                }
                return node;
            }
            else
            {
                return new MetadataTreeNode( pomArtifact, parent, false, query.getArtifactScope() );
            }
        }
        catch ( Exception anyEx )
        {
            throw new MetadataResolutionException( anyEx );
        }
    }
}
