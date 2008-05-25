package org.apache.maven.mercury.representation.conflict;

import org.apache.maven.mercury.representation.MetadataGraphEdge;
import org.apache.maven.mercury.representation.version.ArtifactVersion;
import org.apache.maven.mercury.representation.version.DefaultArtifactVersion;

/*
 * 
 * @plexus.component
 * 
 * @author <a href="mailto:oleg@codehaus.org">Oleg Gusakov</a>
 * 
 * @version $Id$
 */

public class DefaultConflictResolutionPolicy
    implements ConflictResolutionPolicy
{
    /**
     * artifact, closer to the entry point, is selected
     * 
     * @plexus.configuration default-value="true"
     */
    private boolean closerFirst = true;

    /**
     * newer artifact is selected
     * 
     * @plexus.configuration default-value="true"
     */
    private boolean newerFirst = true;

    public MetadataGraphEdge apply( MetadataGraphEdge e1, MetadataGraphEdge e2 )
    {
        int depth1 = e1.getDepth();
        int depth2 = e2.getDepth();

        if ( depth1 == depth2 )
        {
            ArtifactVersion v1 = new DefaultArtifactVersion( e1.getVersion() );
            ArtifactVersion v2 = new DefaultArtifactVersion( e2.getVersion() );

            if ( newerFirst )
            {
                return v1.compareTo( v2 ) > 0 ? e1 : e2;
            }

            return v1.compareTo( v2 ) > 0 ? e2 : e1;
        }

        if ( closerFirst )
        {
            return depth1 < depth2 ? e1 : e2;
        }

        return depth1 < depth2 ? e2 : e1;
    }

}
