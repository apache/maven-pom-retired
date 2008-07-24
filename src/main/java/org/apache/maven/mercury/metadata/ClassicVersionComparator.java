package org.apache.maven.mercury.metadata;

import java.util.Comparator;

import org.apache.maven.mercury.ArtifactMetadata;
import org.apache.maven.mercury.metadata.version.DefaultArtifactVersion;

/**
 * classical version comparator: newer is better by default, but that could 
 * be changed by appropriate constructor
 * 
 * @author Oleg Gusakov
 * @version $Id$
 */
public class ClassicVersionComparator
implements Comparator<MetadataTreeNode>
{
  boolean _newerBetter = true;
  
  public ClassicVersionComparator()
  {
  }

  public ClassicVersionComparator( boolean newerBetter )
  {
    _newerBetter = newerBetter;
  }
  
  public int compare( MetadataTreeNode n1, MetadataTreeNode n2 )
  {
    ArtifactMetadata md1 = n1.getMd();
    DefaultArtifactVersion v1 = new DefaultArtifactVersion( md1.getVersion() );
    
    ArtifactMetadata md2 = n2.getMd();
    DefaultArtifactVersion v2 = new DefaultArtifactVersion( md2.getVersion() );
    
    return _newerBetter ? v1.compareTo(v2) : v2.compareTo(v1) ;
  }
  
}
