package org.apache.maven.mercury.artifact;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class ArtifactMetadataList
{
  List<ArtifactBasicMetadata> _artifacts = new ArrayList<ArtifactBasicMetadata>(8);

  public ArtifactMetadataList( ArtifactBasicMetadata... md )
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
  
  List<ArtifactBasicMetadata> getMetadataList()
  {
    return _artifacts;
  }
}
