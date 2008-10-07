package org.apache.maven.mercury.artifact.version;

import java.util.Comparator;

import org.codehaus.plexus.lang.DefaultLanguage;
import org.codehaus.plexus.lang.Language;

/**
 * version comparator used elsewhere to keep version collections sorted
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class VersionComparator
    implements Comparator<String>
{
  private static final Language _lang = new DefaultLanguage( VersionComparator.class );
  
  public int compare( String v1, String v2 )
  {
    if( v1 == null || v2 == null )
      throw new IllegalArgumentException( _lang.getMessage( "null.version.to.compare", v1,v2 )  );
    
    DefaultArtifactVersion av1 = new DefaultArtifactVersion( v1 );
    DefaultArtifactVersion av2 = new DefaultArtifactVersion( v2 );
    
    return av1.compareTo( av2 );
  }

}
