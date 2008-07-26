package org.apache.maven.mercury.metadata;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.mercury.ArtifactBasicMetadata;
import org.apache.maven.mercury.ArtifactMetadata;
import org.apache.maven.mercury.ArtifactScopeEnum;
import org.apache.maven.mercury.metadata.sat.DefaultSatSolver;
import org.apache.maven.mercury.metadata.sat.SatException;
import org.apache.maven.mercury.repository.LocalRepository;
import org.apache.maven.mercury.repository.RemoteRepository;
import org.apache.maven.mercury.repository.Repository;
import org.apache.maven.mercury.repository.api.RepositoryException;
import org.apache.maven.mercury.repository.api.VirtualRepositoryReader;

/**
 * This is the new entry point into Artifact resolution process.
 * It implements 3-phase 
 * 
 * @author Oleg Gusakov
 * @version $Id$
 */
public class MetadataTree
{
//  private MetadataSource _mdSource;
  
  private Set<MetadataTreeArtifactFilter> _filters;
  private List<Comparator<MetadataTreeNode>> _comparators;
  
  private VirtualRepositoryReader _reader;
  
  MetadataTreeNode _root;
  
  /**
   * creates an instance of MetadataTree. Use this instance to 
   * <ul>
   *    <li>buildTree - process all the dependencies</li>
   *    <li>resolveConflicts</li>
   * <ul>
   * 
   * @param filters - can veto any artifact before it's added to the tree
   * @param comparators - used to define selection policies. If null is passed, 
   * classic comparators - nearest/newest first - will be used. 
   * @param repositories - order is <b>very</b> important. Ordering allows 
   * m2eclipse, for instance, insert a workspace repository
   * @throws RepositoryException
   */
  public MetadataTree(
        Set<MetadataTreeArtifactFilter> filters
      , List<Comparator<MetadataTreeNode>> comparators
      , List<Repository> repositories
                     )
  throws RepositoryException
  {
    this._filters = filters;
    this._comparators = comparators;
    
    // if used does not want to bother.
    // if it's an empty list - user does not want any comparators - so be it
    if( _comparators == null )
    {
      _comparators = new ArrayList<Comparator<MetadataTreeNode>>(2);
      _comparators.add( new ClassicDepthComparator() );
      _comparators.add( new ClassicVersionComparator() );
    }
    
    this._reader = new VirtualRepositoryReader( repositories );
  }
  //-----------------------------------------------------
  public MetadataTreeNode buildTree( ArtifactMetadata startMD )
  throws MetadataTreeException
  {
    if( startMD == null )
      throw new MetadataTreeException( "null start point" );
    
    _reader.init();
    
    _root = createNode( startMD, null, startMD );
    return _root;
  }
  //-----------------------------------------------------
  private MetadataTreeNode createNode( ArtifactBasicMetadata nodeMD, MetadataTreeNode parent, ArtifactBasicMetadata nodeQuery )
  throws MetadataTreeException
  {
    checkForCircularDependency( nodeMD, parent );

    ArtifactMetadata mr;
    
    try
    {
        mr = _reader.readDependencies( nodeMD );
  
        if( mr == null )
          throw new MetadataTreeException( "no result found for " + nodeMD );
        
        MetadataTreeNode node = new MetadataTreeNode( mr, parent, nodeQuery );
    
        List<ArtifactBasicMetadata> dependencies = mr.getDependencies();
        
        if( dependencies == null || dependencies.size() < 1 )
          return node;
        
        Map<ArtifactBasicMetadata, List<ArtifactBasicMetadata>> expandedDeps = _reader.readVersions( dependencies );
        
//        if( expandedDeps == null )
//          return node;
      
        for( ArtifactBasicMetadata md : dependencies )
        {
          List<ArtifactBasicMetadata> versions = expandedDeps.get(  md );
          if( versions == null || versions.size() < 1 )
          {
            if( md.isOptional() )
              continue;
            throw new MetadataTreeException( "did not find non-optional artifact for " + md );
          }
          
          boolean noGoodVersions = true;
          for( ArtifactBasicMetadata ver : versions )
          {
            if( veto( ver, _filters) )
              continue;
            
            MetadataTreeNode kid = createNode( ver, node, md );
            node.addChild( kid );
            
            noGoodVersions = false;
          }
          
          if( noGoodVersions )
          {
            if( md.isOptional() )
              continue;
            throw new MetadataTreeException( "did not find non-optional artifact for " + md );
          }
          
          node.addQuery(md);
      }
    
      return node;
    }
    catch (RepositoryException e)
    {
      throw new MetadataTreeException( e );
    }
  }
  //-----------------------------------------------------
  private void checkForCircularDependency( ArtifactBasicMetadata md, MetadataTreeNode parent )
  throws MetadataTreeCircularDependencyException
  {
    MetadataTreeNode p = parent;
    int count = 0;
    while( p != null )
    {
      count++;
//System.out.println("circ "+md+" vs "+p.md);
      if( md.sameGA(p.md) )
      {
        p = parent;
        StringBuilder sb = new StringBuilder( 128 );
        sb.append( md.toString() );
        while( p!= null )
        {
          sb.append(" <- "+p.md.toString() );

          if( md.sameGA(p.md) )
          {
            throw new MetadataTreeCircularDependencyException("circular dependency "+count + " levels up. "
                + sb.toString() + " <= "+(p.parent == null ? "no parent" : p.parent.md) );
          }
          p = p.parent;
        }
      }
      p = p.parent;
    }
  }
  //-----------------------------------------------------
  private boolean veto(ArtifactBasicMetadata md, Set<MetadataTreeArtifactFilter> filters )
  {
    if( filters != null && filters.size() > 1)
      for( MetadataTreeArtifactFilter filter : filters )
        if( filter.veto(md) )
          return true;
    return false;
  }
  //-----------------------------------------------------
  public List<ArtifactMetadata> resolveConflicts( ArtifactScopeEnum scope )
  throws MetadataTreeException
  {
    if( _root == null )
      throw new MetadataTreeException("null tree");
    
    try
    {
      DefaultSatSolver solver = new DefaultSatSolver( _root, scope );
      
      if( _comparators == null )
        _comparators = new ArrayList<Comparator<MetadataTreeNode>>(2);
      
      if( _comparators.size() < 1 )
      {
        _comparators.add( new ClassicDepthComparator() );
        _comparators.add( new ClassicVersionComparator() );
      }
      solver.applyPolicies( _comparators );

      List<ArtifactMetadata> res = solver.solve();
      
      return res;
    }
    catch (SatException e)
    {
      throw new MetadataTreeException(e);
    }
    
  }
  //-----------------------------------------------------
  //-----------------------------------------------------
}
