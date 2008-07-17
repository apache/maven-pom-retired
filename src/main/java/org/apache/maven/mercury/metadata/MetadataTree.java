package org.apache.maven.mercury.metadata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.apache.maven.mercury.ArtifactScopeEnum;
import org.apache.maven.mercury.metadata.sat.DefaultSatSolver;
import org.apache.maven.mercury.metadata.sat.SatException;
import org.apache.maven.mercury.repository.LocalRepository;
import org.apache.maven.mercury.repository.RemoteRepository;

/**
 * This is the new entry point into Artifact resolution process.
 * It implements 3-phase 
 * 
 * @author Oleg Gusakov
 * @version $Id$
 */
public class MetadataTree
{
  private MetadataSource _mdSource;
  private Set<MetadataTreeArtifactFilter> _filters;
  private List<Comparator<MetadataTreeNode>> _comparators;
  private LocalRepository _localRepository;
  private Set<RemoteRepository> _remoteRepositories;
  
  MetadataTreeNode _root;
  
  public MetadataTree(
        MetadataSource mdSource
      , Set<MetadataTreeArtifactFilter> filters
      , List<Comparator<MetadataTreeNode>> comparators
      , LocalRepository localRepository
      , Set<RemoteRepository> remoteRepositories
                        )
  {
    this._mdSource = mdSource;
    this._filters = filters;
    this._comparators = comparators;
    this._localRepository = localRepository;
    this._remoteRepositories = remoteRepositories;
  }
  //-----------------------------------------------------
  public MetadataTreeNode buildTree( ArtifactMetadata startMD )
  throws MetadataTreeException
  {
    if( startMD == null )
      throw new MetadataTreeException( "null start point" );
    
    if( _mdSource == null )
      throw new MetadataTreeException( "null metadata source" );
    
    if( _localRepository == null )
      throw new MetadataTreeException( "null local repo" );
    
    _root = createNode( startMD, null, startMD );
    return _root;
  }
  //-----------------------------------------------------
  private MetadataTreeNode createNode( ArtifactMetadata nodeMD, MetadataTreeNode parent, ArtifactMetadata nodeQuery )
  throws MetadataTreeException
  {
    checkForCircularDependency( nodeMD, parent );

    MetadataResolution mr;
    
    try
    {
      mr = _mdSource.retrieve( nodeMD, _localRepository, _remoteRepositories );

      if( mr == null || mr.getArtifactMetadata() == null )
        throw new MetadataTreeException( "no result found for " + nodeMD );
      
      MetadataTreeNode node = new MetadataTreeNode( mr.getArtifactMetadata(), parent, nodeQuery );
  
      Collection<ArtifactMetadata> dependencies = mr.getArtifactMetadata().getDependencies();
      
      if( dependencies == null || dependencies.size() < 1 )
        return node;
      
      for( ArtifactMetadata md : dependencies )
      {
        Collection<ArtifactMetadata> versions = _mdSource.expand( md, _localRepository, _remoteRepositories );
        if( versions == null || versions.size() < 1 )
        {
          if( md.isOptional() )
            continue;
          throw new MetadataTreeException( "did not find non-optional artifact for " + md );
        }
        
        boolean noGoodVersions = true;
        for( ArtifactMetadata ver : versions )
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
    catch (MetadataRetrievalException e)
    {
      throw new MetadataTreeException( e );
    }
  }
  //-----------------------------------------------------
  private void checkForCircularDependency( ArtifactMetadata md, MetadataTreeNode parent )
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
  private boolean veto(ArtifactMetadata md, Set<MetadataTreeArtifactFilter> filters )
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
