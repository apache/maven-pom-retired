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
      add( m );
  }

  public ArtifactMetadataList( Collection<ArtifactBasicMetadata> md )
  {
    add( md );
  }

  public ArtifactMetadataList( String... mds )
  {
    for( String m : mds )
      add( new ArtifactBasicMetadata(m) );
  }
  
  public void add( ArtifactBasicMetadata md )
  {
    _artifacts.add( md );
  }
  
  public void add( Collection<ArtifactBasicMetadata> md )
  {
    _artifacts.addAll( md );
  }
  
  public void addGav( String md )
  {
    _artifacts.add( new ArtifactBasicMetadata(md) );
  }
  
  public void addByGav( Collection<String> mds )
  {
    for( String m : mds )
      _artifacts.add( new ArtifactBasicMetadata(m) );
  }
  
  public List<ArtifactBasicMetadata> getMetadataList()
  {
    return _artifacts;
  }
  
  public int size()
  {
    return _artifacts.size();
  }
  
  public boolean isEmpty()
  {
    return _artifacts.isEmpty();
  }
  
  public void clear()
  {
    _artifacts.clear();
  }
}
