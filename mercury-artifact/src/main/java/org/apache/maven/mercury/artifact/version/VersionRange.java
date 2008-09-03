package org.apache.maven.mercury.artifact.version;

import java.util.List;

import org.apache.maven.mercury.artifact.Artifact;
import org.apache.maven.mercury.artifact.Quality;
import org.apache.maven.mercury.artifact.QualityEnum;
import org.apache.maven.mercury.artifact.QualityRange;
import org.codehaus.plexus.i18n.DefaultLanguage;
import org.codehaus.plexus.i18n.Language;

/**
 * Single range implementation, similar to OSGi specification:
 * 
 * [1.2.3, 4.5.6) 1.2.3 <= x < 4.5.6
 * [1.2.3, 4.5.6] 1.2.3 <= x <= 4.5.6
 * (1.2.3, 4.5.6) 1.2.3 < x < 4.5.6
 * (1.2.3, 4.5.6] 1.2.3 < x <= 4.5.6
 * 1.2.3 1.2.3 <= x
 * 
 * @author Oleg Gusakov
 * @version $Id$
 */
public class VersionRange
{
  private static final DefaultArtifactVersion ZERO_VERSION = new DefaultArtifactVersion("0.0.0");
  private static final Language _lang = new DefaultLanguage( VersionRange.class );
  
  QualityRange _toQualityRange = QualityRange.ALL;
  
  DefaultArtifactVersion _fromVersion = ZERO_VERSION;
  boolean _fromInclusive = true;
  
  DefaultArtifactVersion _toVersion;
  boolean _toInclusive = false;
  
  //--------------------------------------------------------------------------------------------
  public VersionRange( String range, QualityRange qRange )
  throws VersionException
  {
    this( range );
    setToQualityRange( qRange );
  }
  //--------------------------------------------------------------------------------------------
  public VersionRange( final String rangeIn )
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
        throw new VersionException("invalid range \""+range+"\"");

      if( range.endsWith("]") )
        _toInclusive = true;
      else if( range.endsWith(")") )
        _toInclusive = false;
      else
        throw new VersionException("invalid range \""+range+"\"");
      
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
        throw new VersionException("invalid range \""+range+"\" - from ° cannot be inclusive");
      
      if( _toVersion == null && _toInclusive )
        throw new VersionException("invalid range \""+range+"\" - to ° cannot be inclusive");
      
    }
    else
    {
      checkForValidCharacters(range);
      _fromVersion = new DefaultArtifactVersion( range );
    }
  }
  //--------------------------------------------------------------------------------------------
  public void setToQualityRange( QualityRange qRange )
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
      
      throw new VersionException( "invalid character '"+c+"' in version \""+v+"\"" );
    }
  }
  //--------------------------------------------------------------------------------------------
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
  
  //--------------------------------------------------------------------------------------------
  /**
   * helpful latest version calculator
   * 
   * @param versions
   * @param noSnapshots
   * @return
   */
  public static final String findLatest( final List<String> versions, final boolean noSnapshots )
  {
    DefaultArtifactVersion tempDav = null;
    DefaultArtifactVersion tempDav2 = null;
    String version = null;

    // find latest
    for( String vn : versions )
    {
      // RELEASE?
      if( noSnapshots && vn.endsWith( Artifact.SNAPSHOT_VERSION ))
        continue;
      
      if( version == null )
      {
        version = vn;
        tempDav = new DefaultArtifactVersion( vn );
        continue;
      }
      
      tempDav2 = new DefaultArtifactVersion( vn );
      if( tempDav2.compareTo( tempDav ) > 0 )
      {
        version = vn;
        tempDav = tempDav2;
      }
    }
    return version;
  }
  //--------------------------------------------------------------------------------------------
  //--------------------------------------------------------------------------------------------
}
