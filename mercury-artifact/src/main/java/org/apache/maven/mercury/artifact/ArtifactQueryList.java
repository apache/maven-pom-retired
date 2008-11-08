package org.apache.maven.mercury.artifact;

import java.util.Collection;

/**
 * 
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class ArtifactQueryList
    extends ArtifactMetadataList
{

  /**
   * @param md
   */
  public ArtifactQueryList( ArtifactBasicMetadata... md )
  {
    super( md );
  }

  /**
   * @param md
   */
  public ArtifactQueryList( Collection<ArtifactBasicMetadata> md )
  {
    super( md );
  }

  /**
   * @param mds
   */
  public ArtifactQueryList( String... mds )
  {
    super( mds );
  }

}
