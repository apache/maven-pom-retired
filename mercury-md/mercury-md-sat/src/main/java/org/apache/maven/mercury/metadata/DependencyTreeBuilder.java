package org.apache.maven.mercury.metadata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;
import org.apache.maven.mercury.artifact.ArtifactMetadata;
import org.apache.maven.mercury.artifact.ArtifactScopeEnum;
import org.apache.maven.mercury.artifact.api.ArtifactListProcessor;
import org.apache.maven.mercury.artifact.version.VersionException;
import org.apache.maven.mercury.builder.api.DependencyProcessor;
import org.apache.maven.mercury.metadata.sat.DefaultSatSolver;
import org.apache.maven.mercury.metadata.sat.SatException;
import org.apache.maven.mercury.repository.api.ArtifactBasicResults;
import org.apache.maven.mercury.repository.api.Repository;
import org.apache.maven.mercury.repository.api.RepositoryException;
import org.apache.maven.mercury.repository.virtual.VirtualRepositoryReader;
import org.apache.maven.mercury.util.Util;
import org.codehaus.plexus.lang.DefaultLanguage;
import org.codehaus.plexus.lang.Language;

/**
 * This is the new entry point into Artifact resolution process.
 * 
 * @author Oleg Gusakov
 * @version $Id: MetadataTree.java 681180 2008-07-30 19:34:16Z ogusakov $
 */
class DependencyTreeBuilder
implements DependencyBuilder
{
  Language _lang = new DefaultLanguage(DependencyTreeBuilder.class);
  
  private Collection<MetadataTreeArtifactFilter> _filters;
  private List<Comparator<MetadataTreeNode>> _comparators;
  private Map<String,ArtifactListProcessor> _processors;
  
  private VirtualRepositoryReader _reader;
  
  Map< String, MetadataTreeNode > existingNodes;
  
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
  protected DependencyTreeBuilder(
        Collection<MetadataTreeArtifactFilter> filters
      , List<Comparator<MetadataTreeNode>> comparators
      , Map<String,ArtifactListProcessor> processors
      , Collection<Repository> repositories
      , DependencyProcessor processor
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
    
    if( processors != null )
      _processors = processors;
    
    this._reader = new VirtualRepositoryReader( repositories, processor );
  }
  //------------------------------------------------------------------------
  public MetadataTreeNode buildTree( ArtifactBasicMetadata startMD )
  throws MetadataTreeException
  {
    if( startMD == null )
      throw new MetadataTreeException( "null start point" );
    
    try
    {
      _reader.setProcessors( _processors );
      _reader.init();
    }
    catch( RepositoryException e )
    {
      throw new MetadataTreeException(e);
    }
    
    existingNodes = new HashMap<String, MetadataTreeNode>(128);
    
    MetadataTreeNode root = createNode( startMD, null, startMD );
    
    return root;
  }
  //-----------------------------------------------------
  private MetadataTreeNode createNode( ArtifactBasicMetadata nodeMD, MetadataTreeNode parent, ArtifactBasicMetadata nodeQuery )
  throws MetadataTreeException
  {
    checkForCircularDependency( nodeMD, parent );

    ArtifactMetadata mr;
    
    MetadataTreeNode existingNode = existingNodes.get( nodeQuery.toString() );
    
    if( existingNode != null )
      return existingNode;
    
    try
    {
        mr = _reader.readDependencies( nodeMD );
  
        if( mr == null )
          throw new MetadataTreeException( _lang.getMessage( "artifact.md.not.found", nodeMD.toString() ) );
        
        MetadataTreeNode node = new MetadataTreeNode( mr, parent, nodeQuery );
    
        List<ArtifactBasicMetadata> dependencies = mr.getDependencies();
        
        if( dependencies == null || dependencies.size() < 1 )
          return node;
        
        ArtifactBasicResults res = _reader.readVersions( dependencies );
        Map<ArtifactBasicMetadata, List<ArtifactBasicMetadata>> expandedDeps = res.getResults();
        
        for( ArtifactBasicMetadata md : dependencies )
        {
          List<ArtifactBasicMetadata> versions = expandedDeps.get( md );
          if( versions == null || versions.size() < 1 )
          {
            if( md.isOptional() )
              continue;
            throw new MetadataTreeException( "did not find non-optional artifact for " + md );
          }
          
          boolean noGoodVersions = true;
          boolean noVersions = true;
          for( ArtifactBasicMetadata ver : versions )
          {
            if( veto( ver, _filters) || vetoInclusionsExclusions(node, ver) )
            {
              // there were good versions, but this one is filtered out filtered out
              noGoodVersions = false;
              continue;
            }
            
            MetadataTreeNode kid = createNode( ver, node, md );
            node.addChild( kid );
            
            noVersions = false;
            
            noGoodVersions = false;
          }
          
          
          if( noVersions && !noGoodVersions )
          {
            // there were good versions, but they were all filtered out
            continue;
          }
          else if( noGoodVersions )
          {
            if( md.isOptional() )
              continue;
            throw new MetadataTreeException( "did not find non-optional artifact for " + md );
          }
          else
            node.addQuery(md);
      }
        
      existingNodes.put( nodeQuery.toString(), node );
    
      return node;
    }
    catch (RepositoryException e)
    {
      throw new MetadataTreeException( e );
    }
    catch( VersionException e )
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
  private boolean veto(ArtifactBasicMetadata md, Collection<MetadataTreeArtifactFilter> filters )
  {
    if( filters != null && filters.size() > 1)
      for( MetadataTreeArtifactFilter filter : filters )
        if( filter.veto(md) )
          return true;
    return false;
  }
  //-----------------------------------------------------
  private boolean vetoInclusionsExclusions( MetadataTreeNode node, ArtifactBasicMetadata ver )
  throws VersionException
  {
    for( MetadataTreeNode n = node; n != null; n = n.getParent() )
    {
      ArtifactBasicMetadata md = n.getQuery();
      
      if( md.allowDependency( ver ) )
        return false;
    }
    return true;
  }
  //-----------------------------------------------------
  public List<ArtifactMetadata> resolveConflicts( MetadataTreeNode root, ArtifactScopeEnum scope )
  throws MetadataTreeException
  {
    if( root == null )
      throw new MetadataTreeException(_lang.getMessage( "empty.tree" ));
    
    try
    {
      DefaultSatSolver solver = new DefaultSatSolver( root, scope );
      
      solver.applyPolicies( getComparators() );

      List<ArtifactMetadata> res = solver.solve();
      
      return res;
    }
    catch (SatException e)
    {
      throw new MetadataTreeException(e);
    }
    
  }
  //-----------------------------------------------------
  private List<Comparator<MetadataTreeNode>> getComparators()
  {
    if( Util.isEmpty( _comparators ) )
      _comparators = new ArrayList<Comparator<MetadataTreeNode>>(2);
    
    if( _comparators.size() < 1 )
    {
      _comparators.add( new ClassicDepthComparator() );
      _comparators.add( new ClassicVersionComparator() );
    }
    
    return _comparators;
  }
  //-----------------------------------------------------
  public List<ArtifactMetadata> resolveConflicts( List<ArtifactBasicMetadata>trees, ArtifactScopeEnum scope )
  throws MetadataTreeException
  {
    if( Util.isEmpty( trees ) )
      throw new MetadataTreeException(_lang.getMessage( "empty.tree.collection" ));
    
    String dummyGAV = "__fake:__fake:0.0.0";
    
    ArtifactBasicMetadata query = new ArtifactBasicMetadata( dummyGAV );
    
    ArtifactMetadata dummyMd = new ArtifactMetadata( query );
    dummyMd.setDependencies( trees );
    
    MetadataTreeNode root = new MetadataTreeNode( dummyMd, null, query );
    
    try
    {
      DefaultSatSolver solver = new DefaultSatSolver( root, scope );
      
      solver.applyPolicies( getComparators() );

      List<ArtifactMetadata> res = solver.solve();
      
      res.remove( dummyMd );
      
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
