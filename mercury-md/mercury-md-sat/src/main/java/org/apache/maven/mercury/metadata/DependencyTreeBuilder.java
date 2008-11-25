/**
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
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
import org.apache.maven.mercury.artifact.version.VersionRange;
import org.apache.maven.mercury.artifact.version.VersionRangeFactory;
import org.apache.maven.mercury.event.EventGenerator;
import org.apache.maven.mercury.event.EventManager;
import org.apache.maven.mercury.event.EventTypeEnum;
import org.apache.maven.mercury.event.GenericEvent;
import org.apache.maven.mercury.event.MercuryEvent;
import org.apache.maven.mercury.event.MercuryEventListener;
import org.apache.maven.mercury.logging.IMercuryLogger;
import org.apache.maven.mercury.logging.MercuryLoggerManager;
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
 * @version $Id$
 */
class DependencyTreeBuilder
implements DependencyBuilder, EventGenerator
{
  private static final Language _lang = new DefaultLanguage(DependencyTreeBuilder.class);
  private static final IMercuryLogger _log = MercuryLoggerManager.getLogger( DependencyTreeBuilder.class ); 
  
  private Collection<MetadataTreeArtifactFilter> _filters;
  private List<Comparator<MetadataTreeNode>> _comparators;
  private Map<String,ArtifactListProcessor> _processors;
  
  private VirtualRepositoryReader _reader;
  
  private Map< String, MetadataTreeNode > _existingNodes;
  
  private EventManager _eventManager;
  
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
        Collection<Repository> repositories
      , Collection<MetadataTreeArtifactFilter> filters
      , List<Comparator<MetadataTreeNode>> comparators
      , Map<String,ArtifactListProcessor> processors
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
    
    this._reader = new VirtualRepositoryReader( repositories );
  }
  //------------------------------------------------------------------------
  public MetadataTreeNode buildTree( ArtifactBasicMetadata startMD, ArtifactScopeEnum treeScope )
  throws MetadataTreeException
  {
    if( startMD == null )
      throw new MetadataTreeException( "null start point" );
    
    try
    {
      _reader.setEventManager( _eventManager );
      _reader.setProcessors( _processors );
      _reader.init();
    }
    catch( RepositoryException e )
    {
      throw new MetadataTreeException(e);
    }
    
    _existingNodes = new HashMap<String, MetadataTreeNode>(256);
    
    GenericEvent treeBuildEvent = new GenericEvent( EventTypeEnum.dependencyBuilder, TREE_BUILD_EVENT, startMD.getGAV() );
    
    MetadataTreeNode root = createNode( startMD, null, startMD, treeScope );
    
    treeBuildEvent.stop();
    
    if( _eventManager != null )
      _eventManager.fireEvent( treeBuildEvent );
    
    MetadataTreeNode.reNumber( root, 1 );
    
    return root;
  }
  //-----------------------------------------------------
  private MetadataTreeNode createNode( ArtifactBasicMetadata nodeMD, MetadataTreeNode parent, ArtifactBasicMetadata nodeQuery, ArtifactScopeEnum globalScope )
  throws MetadataTreeException
  {
    GenericEvent nodeBuildEvent = null;
    
    if( _eventManager != null )
      nodeBuildEvent = new GenericEvent( EventTypeEnum.dependencyBuilder, TREE_NODE_BUILD_EVENT, nodeMD.getGAV() );
    
    try
    {
      checkForCircularDependency( nodeMD, parent );
  
      ArtifactMetadata mr;
      
      MetadataTreeNode existingNode = _existingNodes.get( nodeQuery.toString() );
      
      if( existingNode != null )
        return MetadataTreeNode.deepCopy( existingNode );
    
        mr = _reader.readDependencies( nodeMD );
  
        if( mr == null )
          throw new MetadataTreeException( _lang.getMessage( "artifact.md.not.found", nodeMD.toString() ) );
        
        MetadataTreeNode node = new MetadataTreeNode( mr, parent, nodeQuery );
    
        List<ArtifactBasicMetadata> allDependencies = mr.getDependencies();
        
        if( allDependencies == null || allDependencies.size() < 1 )
          return node;
        
        List<ArtifactBasicMetadata> dependencies = new ArrayList<ArtifactBasicMetadata>( allDependencies.size() );
        if( globalScope != null )
          for( ArtifactBasicMetadata md : allDependencies )
          {
            ArtifactScopeEnum mdScope = md.getArtifactScope(); 
            if( globalScope.encloses( mdScope ) )
              dependencies.add( md );
          }
        else
          dependencies.addAll( allDependencies );
        
        if( Util.isEmpty( dependencies ) )
          return node;

        ArtifactBasicResults res = _reader.readVersions( dependencies );

        Map<ArtifactBasicMetadata, List<ArtifactBasicMetadata>> expandedDeps = res.getResults();
        
        for( ArtifactBasicMetadata md : dependencies )
        {

if( _log.isDebugEnabled() )
  _log.debug("node "+nodeQuery+", dep "+md );

          List<ArtifactBasicMetadata> versions = expandedDeps.get( md );
          if( versions == null || versions.size() < 1 )
          {
            if( md.isOptional() )
              continue;
            
            throw new MetadataTreeException( "did not find non-optional artifact for " + md + " <== " + showPath( node ) );
          }
          
          boolean noGoodVersions = true;
          boolean noVersions = true;
          for( ArtifactBasicMetadata ver : versions )
          {
            if( veto( ver, _filters) || vetoInclusionsExclusions(node, ver) )
            {
              // there were good versions, but this one is filtered out
              noGoodVersions = false;
              continue;
            }
            
            MetadataTreeNode kid = createNode( ver, node, md, globalScope );
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
        
      _existingNodes.put( nodeQuery.toString(), node );
    
      return node;
    }
    catch (RepositoryException e)
    {
      if( _eventManager != null )
        nodeBuildEvent.setResult( e.getMessage() );

      throw new MetadataTreeException( e );
    }
    catch( VersionException e )
    {
      if( _eventManager != null )
        nodeBuildEvent.setResult( e.getMessage() );

      throw new MetadataTreeException( e );
    }
    catch( MetadataTreeException e )
    {
      if( _eventManager != null )
        nodeBuildEvent.setResult( e.getMessage() );
      throw e;
    }
    finally
    {
      if( _eventManager != null )
      {
        nodeBuildEvent.stop();
        _eventManager.fireEvent( nodeBuildEvent );
      }
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
      
      if( !md.allowDependency( ver ) ) // veto it
        return true;
    }
    return false; // allow because all parents are OK with it
  }
  //-----------------------------------------------------
  public List<ArtifactMetadata> resolveConflicts( MetadataTreeNode root )
  throws MetadataTreeException
  {
    if( root == null )
      throw new MetadataTreeException(_lang.getMessage( "empty.tree" ));
    
    try
    {
      DefaultSatSolver solver = new DefaultSatSolver( root, _eventManager );
      
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
  public MetadataTreeNode resolveConflictsAsTree( MetadataTreeNode root )
  throws MetadataTreeException
  {
    if( root == null )
      throw new MetadataTreeException(_lang.getMessage( "empty.tree" ));
    
    try
    {
      DefaultSatSolver solver = new DefaultSatSolver( root, _eventManager );
      
      solver.applyPolicies( getComparators() );

      MetadataTreeNode res = solver.solveAsTree();
      
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
  private List<ArtifactMetadata> resolveConflicts( List<ArtifactBasicMetadata>trees )
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
      DefaultSatSolver solver = new DefaultSatSolver( root, _eventManager );
      
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
  private String showPath( MetadataTreeNode node )
  throws MetadataTreeCircularDependencyException
  {
    StringBuilder sb = new StringBuilder( 256 );
    
    String comma = "";
    
    MetadataTreeNode p = node;

    while( p != null )
    {
      sb.append( comma + p.getMd().toString() );
      
      comma = " <== ";
      
      p = p.parent;
    }
    
    return sb.toString();
  }

  public void register( MercuryEventListener listener )
  {
    if( _eventManager == null )
      _eventManager = new EventManager();
      
    _eventManager.register( listener );
  }

  public void unRegister( MercuryEventListener listener )
  {
    if( _eventManager != null )
      _eventManager.unRegister( listener );
  }
  
  public void setEventManager( EventManager eventManager )
  {
    if( _eventManager == null )
      _eventManager = eventManager;
    else
      _eventManager.getListeners().addAll( eventManager.getListeners() );
      
  }
}
