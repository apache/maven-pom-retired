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
  boolean _optional;
  //---------------------------------------------------------------------
  public SatVar( ArtifactMetadata md, int var, boolean optional )
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
    this._optional = optional;
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

  public boolean isWeak()
  {
    return this._optional;
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
