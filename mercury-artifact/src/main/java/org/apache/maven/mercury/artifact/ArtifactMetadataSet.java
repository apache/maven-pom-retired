package org.apache.maven.mercury.artifact;

import java.util.ArrayList;
import java.util.Collection;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class ArtifactMetadataSet
{
  Collection<ArtifactBasicMetadata> _artifacts = new ArrayList<ArtifactBasicMetadata>(8);

  public ArtifactMetadataSet( ArtifactBasicMetadata... md )
  {
    for( ArtifactBasicMetadata m : md )
      _artifacts.add( m );
  }
  
  public void add( ArtifactBasicMetadata md )
  {
    _artifacts.add( md );
  }
  
  public void add( Collection<ArtifactBasicMetadata> md )
  {
    _artifacts.addAll( md );
  }
  
  Collection<ArtifactBasicMetadata> getMetadatas()
  {
    return _artifacts;
  }
}
