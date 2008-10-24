package org.apache.maven.mercury.artifact.version;

import java.util.Comparator;

import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;
import org.codehaus.plexus.lang.DefaultLanguage;
import org.codehaus.plexus.lang.Language;

/**
 * version comparator used elsewhere to keep version collections sorted
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class MetadataVersionComparator
implements Comparator<ArtifactBasicMetadata>
{
  private static final Language _lang = new DefaultLanguage( MetadataVersionComparator.class );
  
  public int compare( ArtifactBasicMetadata v1, ArtifactBasicMetadata v2 )
  {
    if( v1 == null || v2 == null )
      throw new IllegalArgumentException( _lang.getMessage( "null.version.to.compare", v1 == null ? "null" : v1.toString(), v2 == null ? "null" : v2.toString() )  );
    
    DefaultArtifactVersion av1 = new DefaultArtifactVersion( v1.getVersion() );
    DefaultArtifactVersion av2 = new DefaultArtifactVersion( v2.getVersion() );
    
    return av1.compareTo( av2 );
  }

}
