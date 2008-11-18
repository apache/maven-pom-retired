package org.apache.maven.mercury.metadata.sat;

import org.apache.maven.mercury.artifact.ArtifactMetadata;
import org.apache.maven.mercury.metadata.MetadataTreeNode;

/**
 * @author <a href="oleg@codehaus.org">Oleg Gusakov</a>
 */
class SatVar
{
  MetadataTreeNode _node;
  int _literal;
  boolean _optional;
  //---------------------------------------------------------------------
  public SatVar( MetadataTreeNode n )
  throws SatException
  {
    if( n == null
        || n.getMd() == null
    )
      throw new SatException("Cannot create SatVar from a null MetadataTreeNode: "+n);
    
    ArtifactMetadata md = n.getMd();
    if(    
       md == null
      || md.getGroupId() == null
      || md.getArtifactId() == null
      || md.getVersion() == null
    )
      throw new SatException("Cannot create SatVar from a null Metadata: "+md);

    this._node = n;
    this._literal = n.getId();
  }
  //---------------------------------------------------------------------
  public ArtifactMetadata getMd()
  {
    return _node.getMd();
  }

  public int getLiteral()
  {
    return _literal;
  }
  //---------------------------------------------------------------------
  @Override
  public String toString()
  {
    return _node.toString()+" -> X"+_literal;
  }
  
  //---------------------------------------------------------------------
  //---------------------------------------------------------------------
}
