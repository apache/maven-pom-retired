package org.apache.maven.mercury.metadata;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;
import org.apache.maven.mercury.artifact.ArtifactMetadata;
import org.apache.maven.mercury.artifact.ArtifactScopeEnum;
import org.apache.maven.mercury.logging.IMercuryLogger;
import org.apache.maven.mercury.logging.MercuryLoggerManager;
import org.apache.maven.mercury.metadata.sat.DefaultSatSolver;
import org.codehaus.plexus.lang.DefaultLanguage;
import org.codehaus.plexus.lang.Language;
/**
 * metadata [dirty] Tree
 * 
 * @author <a href="oleg@codehaus.org">Oleg Gusakov</a>
 *
 */
public class MetadataTreeNode
{
  private static final int DEFAULT_CHILDREN_COUNT = 8;
  
  private static final IMercuryLogger _log = MercuryLoggerManager.getLogger( MetadataTreeNode.class ); 
  private static final Language _lang = new DefaultLanguage( MetadataTreeNode.class );
  
  /**
   * this node's artifact MD
   */
  ArtifactMetadata md;
  
  /**
   * fail resolution if it could not be found?
   */
  boolean optional = false;

  /**
   * parent node
   */
  MetadataTreeNode parent;
  
  /**
   * query node - the one that originated this actual node
   */
  ArtifactBasicMetadata query;

  /**
   * queries - one per POM dependency
   */
  List<ArtifactBasicMetadata> queries;

  /**
   * actual found versions
   */
  List<MetadataTreeNode> children;
  //------------------------------------------------------------------------
  public int countNodes()
  {
    return countNodes(this);
  }
  //------------------------------------------------------------------------
  public static int countNodes( MetadataTreeNode node )
  {
    int res = 1;
    
    if( node.children != null && node.children.size() > 0)
    {
      for( MetadataTreeNode child : node.children )
      {
        res += countNodes( child );
      }
    }
    
    return res;
  }
  //------------------------------------------------------------------------
  public int countDistinctNodes()
  {
    TreeSet<String> nodes = new TreeSet<String>();
    
    getDistinctNodes( this, nodes );
if( _log.isDebugEnabled() )
{
  _log.debug( "tree distinct nodes count" );
  _log.debug( nodes.toString() );
}

    return nodes.size();
  }
  //------------------------------------------------------------------------
  public static void getDistinctNodes( MetadataTreeNode node, TreeSet<String> nodes )
  {
    if( node.getMd() == null )
      throw new IllegalArgumentException( "tree node without metadata" );
    
    nodes.add( node.getMd().getGAV() );
    
    if( node.children != null && node.children.size() > 0)
      for( MetadataTreeNode child : node.children )
        getDistinctNodes( child, nodes );
  }
	//------------------------------------------------------------------------
  public MetadataTreeNode()
  {
  }
  //------------------------------------------------------------------------
  /**
   * pointers to parent and query are a must. 
   */
  public MetadataTreeNode(   ArtifactMetadata md,
                             MetadataTreeNode parent,
                             ArtifactBasicMetadata query,
                             boolean resolved,
                             ArtifactScopeEnum scope
                         )
  {
        if ( md != null )
        {
            md.setArtifactScope( ArtifactScopeEnum.checkScope(scope) );
            md.setResolved(resolved);
        }

        this.md = md;
        this.parent = parent;
        this.query = query;
  }
  //------------------------------------------------------------------------
  public MetadataTreeNode( ArtifactMetadata md, MetadataTreeNode parent, ArtifactBasicMetadata query )
  {
    this( md, parent, query, true, ArtifactScopeEnum.compile );
  }
  //------------------------------------------------------------------------
  /**
   * dependencies are ordered in the POM - they should be added in the POM order
   */
  public MetadataTreeNode addChild( MetadataTreeNode kid )
  {
      if ( kid == null )
      {
          return this;
      }

      if( children == null )
      {
      	children = new ArrayList<MetadataTreeNode>( DEFAULT_CHILDREN_COUNT );
      }
              
      kid.setParent( this );
      children.add( kid );
      
      return this;
  }
  //------------------------------------------------------------------------
  /**
   * dependencies are ordered in the POM - they should be added in the POM order
   */
  public MetadataTreeNode addQuery( ArtifactBasicMetadata query )
  {
      if ( query == null )
      {
          return this;
      }

      if( queries == null )
      {
        queries = new ArrayList<ArtifactBasicMetadata>( DEFAULT_CHILDREN_COUNT );
      }
              
      queries.add( query );
      
      return this;
  }
    //------------------------------------------------------------------
    @Override
    public String toString()
    {
        return md == null 
            ? "no metadata, parent " + 
                ( parent == null ? "null" : parent.toString() ) 
            : md.toString()+":d="+getDepth()
            ;
    }
    //------------------------------------------------------------------------
    public boolean hasChildren()
    {
        return children != null;
    }
    //------------------------------------------------------------------------
    public ArtifactMetadata getMd()
    {
        return md;
    }

    public MetadataTreeNode getParent()
    {
        return parent;
    }

    public int getDepth()
    {
      int depth = 0;
      
      for( MetadataTreeNode p = parent; p != null; p = p.parent )
        ++depth;
      
      return depth;
    }

    public int getMaxDepth( int depth )
    {
      int res = 0;
      
      if( ! hasChildren() )
        return depth + 1;
      
      for( MetadataTreeNode kid : children )
      {
        int kidDepth = kid.getMaxDepth( depth + 1 );
        if( kidDepth > res )
          res = kidDepth;
      }
      
      return res;
    }

    public void setParent( MetadataTreeNode parent )
    {
        this.parent = parent;
    }

    public List<MetadataTreeNode> getChildren()
    {
        return children;
    }

    public boolean isOptional()
    {
        return optional;
    }
    
    public ArtifactBasicMetadata getQuery()
    {
      return query;
    }
    
    public List<ArtifactBasicMetadata> getQueries()
    {
      return queries;
    }
    //------------------------------------------------------------------------
    //------------------------------------------------------------------------

}
