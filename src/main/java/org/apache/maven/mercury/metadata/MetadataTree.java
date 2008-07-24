package org.apache.maven.mercury.metadata;

import java.util.ArrayList;
import java.util.Collection;
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
import org.apache.maven.mercury.repository.api.RepositoryException;
import org.apache.maven.mercury.repository.api.RepositoryReader;
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
  
  public MetadataTree(
        Set<MetadataTreeArtifactFilter> filters
      , List<Comparator<MetadataTreeNode>> comparators
      , LocalRepository localRepository
      , List<RemoteRepository> remoteRepositories
                     )
  throws RepositoryException
  {
    this._filters = filters;
    this._comparators = comparators;
    this._reader = new VirtualRepositoryReader( localRepository, remoteRepositories );
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
  private MetadataTreeNode createNode( ArtifactBasicMetadata nodeMD, MetadataTreeNode parent, ArtifactMetadata nodeQuery )
  throws MetadataTreeException
  {
    checkForCircularDependency( nodeMD, parent );

    ArtifactMetadata mr;
    
    try
    {
      mr = _reader.readMetadata( nodeMD );

      if( mr == null )
        throw new MetadataTreeException( "no result found for " + nodeMD );
      
      MetadataTreeNode node = new MetadataTreeNode( mr, parent, nodeQuery );
  
      List<ArtifactMetadata> dependencies = mr.getDependencies();
      
      if( dependencies == null || dependencies.size() < 1 )
        return node;
      
      Map<ArtifactBasicMetadata, List<ArtifactBasicMetadata>> expandedDeps = _reader.findMetadata( dependencies );
      
      for( ArtifactMetadata md : dependencies )
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
