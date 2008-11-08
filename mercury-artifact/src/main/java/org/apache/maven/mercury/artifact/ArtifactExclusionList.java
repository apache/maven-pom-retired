package org.apache.maven.mercury.artifact;

import java.util.Collection;

/**
 * 
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class ArtifactExclusionList
    extends ArtifactMetadataList
{

  /**
   * @param md
   */
  public ArtifactExclusionList( ArtifactBasicMetadata... md )
  {
    super( md );
  }

  /**
   * @param md
   */
  public ArtifactExclusionList( Collection<ArtifactBasicMetadata> md )
  {
    super( md );
  }

  /**
   * @param mds
   */
  public ArtifactExclusionList( String... mds )
  {
    super( mds );
  }

}
