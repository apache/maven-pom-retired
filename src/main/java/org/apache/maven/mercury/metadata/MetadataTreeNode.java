package org.apache.maven.mercury.metadata;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.mercury.Artifact;
import org.apache.maven.mercury.ArtifactScopeEnum;
/**
 * metadata [dirty] Tree
 * 
 * @author <a href="oleg@codehaus.org">Oleg Gusakov</a>
 *
 */

public class MetadataTreeNode
{
  private static final int DEFAULT_CHILDREN_COUNT = 8;
  
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
  ArtifactMetadata query;

  /**
   * queries - one per POM dependency
   */
  List<ArtifactMetadata> queries;

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
  public MetadataTreeNode()
  {
  }
  //------------------------------------------------------------------------
  /**
   * pointers to parent and query are a must. 
   */
  public MetadataTreeNode(   ArtifactMetadata md,
                             MetadataTreeNode parent,
                             ArtifactMetadata query,
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
  public MetadataTreeNode( ArtifactMetadata md, MetadataTreeNode parent, ArtifactMetadata query )
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
  public MetadataTreeNode addQuery( ArtifactMetadata query )
  {
      if ( query == null )
      {
          return this;
      }

      if( queries == null )
      {
        queries = new ArrayList<ArtifactMetadata>( DEFAULT_CHILDREN_COUNT );
      }
              
      queries.add( query );
      
      return this;
  }
    //------------------------------------------------------------------
    @Override
    public String toString()
    {
        return md == null 
            ? "no metadata, parent " + ( parent == null ? "null"
            : parent.toString() ) : md.toString()
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

    public void setMd( ArtifactMetadata md )
    {
        this.md = md;
    }

    public MetadataTreeNode getParent()
    {
        return parent;
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
    
    public void setOptional( boolean optional )
    {
      this.optional = optional;
    }
    public ArtifactMetadata getQuery()
    {
      return query;
    }
    public void setQuery(ArtifactMetadata query)
    {
      this.query = query;
    }
    public List<ArtifactMetadata> getQueries()
    {
      return queries;
    }
    public void setQueries(List<ArtifactMetadata> queries)
    {
      this.queries = queries;
    }
    public void setChildren(List<MetadataTreeNode> children)
    {
      this.children = children;
    }
    //------------------------------------------------------------------------
    //------------------------------------------------------------------------

}
