package org.apache.maven.mercury.artifact.version;

import org.apache.maven.mercury.artifact.Quality;
import org.apache.maven.mercury.artifact.QualityEnum;
import org.apache.maven.mercury.artifact.QualityRange;
import org.codehaus.plexus.lang.DefaultLanguage;
import org.codehaus.plexus.lang.Language;

/**
 * Single range implementation, similar to OSGi specification:
 * 
 * [1.2.3, 4.5.6) 1.2.3 <= x < 4.5.6
 * [1.2.3, 4.5.6] 1.2.3 <= x <= 4.5.6
 * (1.2.3, 4.5.6) 1.2.3 < x < 4.5.6
 * (1.2.3, 4.5.6] 1.2.3 < x <= 4.5.6
 * 1.2.3 1.2.3 <= x - this one is configurable nowadays
 * 
 * @author Oleg Gusakov
 * @version $Id$
 */
class MavenVersionRange
implements VersionRange
{
  public static final String SYSTEM_PARAMETER_OSGI_VERSION = "maven.mercury.osgi.version";
  public static final String SYSTEM_PARAMETER_OSGI_VERSION_DEFAULT = "false";
  private boolean _osgiVersion = Boolean.parseBoolean( System.getProperty( SYSTEM_PARAMETER_OSGI_VERSION, SYSTEM_PARAMETER_OSGI_VERSION_DEFAULT ) );

  private static final DefaultArtifactVersion ZERO_VERSION = new DefaultArtifactVersion("0.0.0");
  private static final Language _lang = new DefaultLanguage( MavenVersionRange.class );
  
  QualityRange _toQualityRange = QualityRange.ALL;
  
  DefaultArtifactVersion _fromVersion = ZERO_VERSION;
  boolean _fromInclusive = true;
  
  DefaultArtifactVersion _toVersion;
  boolean _toInclusive = false;
  
  //--------------------------------------------------------------------------------------------
  protected MavenVersionRange( final String range, final QualityRange qRange )
  throws VersionException
  {
    this( range );
    setToQualityRange( qRange );
  }
  //--------------------------------------------------------------------------------------------
  protected MavenVersionRange( final String rangeIn )
  throws VersionException
  {
    String range = AttributeQuery.stripExpression( rangeIn );

    if( range == null || range.length() < 1 )
      return;    
    
    if( range.indexOf(',') > 0 )
    {
      if( range.startsWith("[") )
        _fromInclusive = true;
      else if( range.startsWith("(") )
        _fromInclusive = false;
      else
        throw new VersionException( _lang.getMessage( "invalid.maven.version.range", range ) );

      if( range.endsWith("]") )
        _toInclusive = true;
      else if( range.endsWith(")") )
        _toInclusive = false;
      else
        throw new VersionException( _lang.getMessage( "invalid.maven.version.range", range ) );
      
      int ind = range.indexOf(',');

      String sFrom = range.substring(1,ind);
      if( sFrom != null && sFrom.length() > 0 )
      {
        String sFromT = sFrom.trim();
        if( sFromT != null && sFromT.length() > 0 )
        {
          checkForValidCharacters( sFromT );
          // TODO og: look for LATEST,RELEASE and SNAPSHOT
          Quality vq = new Quality( sFromT );
          if( vq.getQuality().equals( QualityEnum.snapshot )
              || vq.getQuality().equals( QualityEnum.unknown )
          )
              throw new VersionException( _lang.getMessage( "bad.version.sn", sFromT ) );
          
          _fromVersion = new DefaultArtifactVersion( sFromT );
        }
      }

      String sTo = range.substring( ind+1, range.length()-1 );
      if( sTo != null && sTo.length() > 0 )
      {
        String sToT = sTo.trim();
        if( sToT != null && sToT.length() > 0 )
        {
          checkForValidCharacters( sToT );
          _toVersion = new DefaultArtifactVersion( sToT );
        }
      }
      
      if( _fromVersion == null && _fromInclusive )
        throw new VersionException( _lang.getMessage( "invalid.maven.version.range.bad.from", range ) );
      
      if( _toVersion == null && _toInclusive )
        throw new VersionException( _lang.getMessage( "invalid.maven.version.range.bad.to", range ) );
      
    }
    else
    {
      checkForValidCharacters(range);
      _fromVersion = new DefaultArtifactVersion( range );
      
      // good old maven version interpretation
      if( !_osgiVersion )
      {
        _toVersion = _fromVersion;
        _fromInclusive = true;
        _toInclusive   = true;
      }
    }
  }
  //--------------------------------------------------------------------------------------------
  protected void setToQualityRange( QualityRange qRange )
  {
    this._toQualityRange = qRange;
  }
  //--------------------------------------------------------------------------------------------
  private void checkForValidCharacters( String v )
  throws VersionException
  {
    if( v == null || v.length() < 1 )
      throw new VersionException("empty version");
    
    int len = v.length();

    for( int i=0; i<len; i++ )
    {
      char c = v.charAt(0);
      
      if( c >= '0' && c <= '9' )
        continue;
      
      if( c >= 'A' && c <= 'Z' )
        continue;
      
      if( c >= 'a' && c <= 'z' )
        continue;
      
      if( c == '-' || c == '_' )
        continue;
      
      throw new VersionException( _lang.getMessage( "invalid.character", ""+c, v ) );
    }
  }
  //--------------------------------------------------------------------------------------------
  /* (non-Javadoc)
   * @see org.apache.maven.mercury.artifact.version.VersionRange#includes(java.lang.String)
   */
  public boolean includes( String version )
  {
    DefaultArtifactVersion ver = new DefaultArtifactVersion( version );
    
    int cmp1 = ver.compareTo( _fromVersion );
    
    if( cmp1 < 0 )
      return false;
    
    if( cmp1 == 0 )
      return _fromInclusive;
    
    if( _toVersion == null ) // eternity
      return true;
    
    int cmp2 = ver.compareTo( _toVersion );
    
    if( cmp2 < 0 )
    {
      if( ver.sameBase( _toVersion ) )
      {
        if( _toQualityRange.isAcceptedQuality( ver.getQuality() ) )
          return true;
        return false;
      }
      return true;
    }
    
    if( cmp2 == 0 )
      return _toInclusive;
    
    return false;
  }

  public void setOption( String name, String val )
  {
    if( SYSTEM_PARAMETER_OSGI_VERSION.equals( name ) )
      _osgiVersion = Boolean.parseBoolean( System.getProperty( val, SYSTEM_PARAMETER_OSGI_VERSION_DEFAULT ) );
  }
  
  //--------------------------------------------------------------------------------------------
  //--------------------------------------------------------------------------------------------
}
