package org.apache.maven.mercury.metadata.sat;

import org.apache.maven.mercury.metadata.ArtifactMetadata;

/**
 * @author <a href="oleg@codehaus.org">Oleg Gusakov</a>
 */
class SatVar
implements Comparable<ArtifactMetadata>
{
  ArtifactMetadata _md;
  int _no;
  //---------------------------------------------------------------------
  public SatVar( ArtifactMetadata md, int var )
  throws SatException
  {
    if( md == null
        || md.getGroupId() == null
        || md.getArtifactId() == null
        || md.getVersion() == null
    )
      throw new SatException("Cannot create SatVar from a null Metadata: "+md);

    this._md = md;
    this._no = var;
  }
  //---------------------------------------------------------------------
  public boolean sameGAV( ArtifactMetadata md )
  {
    if( md == null )
      return false;
    
    return 
        sameGA( md )
        && this._md.getVersion().equals( md.getVersion() )
    ;
  }
  //---------------------------------------------------------------------
  public boolean sameGA( ArtifactMetadata md )
  {
    if( md == null )
      return false;
    
    return 
        this._md.getGroupId().equals( md.getGroupId() )
        && this._md.getArtifactId().equals( md.getArtifactId() )
    ;
  }
  //---------------------------------------------------------------------
  public ArtifactMetadata getMd()
  {
    return _md;
  }
  public void setMd(ArtifactMetadata md)
  {
    this._md = md;
  }
  public int getNo()
  {
    return _no;
  }
  public void setNo(int var)
  {
    this._no = var;
  }
  //---------------------------------------------------------------------
  public int compareTo(ArtifactMetadata md)
  {
    if( md == null
        || md.getGroupId() == null
        || md.getArtifactId() == null
        || md.getVersion() == null
    )
      return -1;
    
    int g = this._md.getGroupId().compareTo( md.getGroupId() );
    if( g == 0 )
    {
      int a = this._md.getArtifactId().compareTo( md.getArtifactId() );
      if( a == 0 )
        return this._md.getVersion().compareTo( md.getVersion() );
      else
        return a;
    }

    return g;
  }
  //---------------------------------------------------------------------
  //---------------------------------------------------------------------
}
