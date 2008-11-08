package org.apache.maven.mercury.artifact;

import java.util.Collection;

/**
 * 
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class ArtifactInclusionList
    extends ArtifactMetadataList
{

  /**
   * @param md
   */
  public ArtifactInclusionList( ArtifactBasicMetadata... md )
  {
    super( md );
  }

  /**
   * @param md
   */
  public ArtifactInclusionList( Collection<ArtifactBasicMetadata> md )
  {
    super( md );
  }

  /**
   * @param mds
   */
  public ArtifactInclusionList( String... mds )
  {
    super( mds );
  }

}
