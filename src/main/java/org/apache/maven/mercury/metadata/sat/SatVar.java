package org.apache.maven.mercury.metadata.sat;

import org.apache.maven.mercury.ArtifactMetadata;

/**
 * @author <a href="oleg@codehaus.org">Oleg Gusakov</a>
 */
class SatVar
implements Comparable<ArtifactMetadata>
{
  ArtifactMetadata _md;
  int _literal;
  boolean _optional;
  //---------------------------------------------------------------------
  public SatVar( ArtifactMetadata md, int literal )
  throws SatException
  {
    if( md == null
        || md.getGroupId() == null
        || md.getArtifactId() == null
        || md.getVersion() == null
    )
      throw new SatException("Cannot create SatVar from a null Metadata: "+md);

    this._md = md;
    this._literal = literal;
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
  public int getLiteral()
  {
    return _literal;
  }
  public void setNo(int var)
  {
    this._literal = var;
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
