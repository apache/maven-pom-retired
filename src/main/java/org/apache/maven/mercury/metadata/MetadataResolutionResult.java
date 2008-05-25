package org.apache.maven.mercury.metadata;

import org.apache.maven.mercury.ArtifactScopeEnum;
import org.apache.maven.mercury.metadata.conflict.ConflictResolutionException;
import org.apache.maven.mercury.metadata.conflict.ConflictResolver;
import org.apache.maven.mercury.metadata.transform.ClasspathContainer;
import org.apache.maven.mercury.metadata.transform.ClasspathTransformation;
import org.apache.maven.mercury.metadata.transform.MetadataGraphTransformationException;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

/** 
 * This object is tinted with ClasspathTransformation and GraphConflictResolver. 
 * Get rid of them after debugging
 * 
 * @author <a href="oleg@codehaus.org">Oleg Gusakov</a>
 */
public class MetadataResolutionResult
{
    MetadataTreeNode treeRoot;
    
    /** 
     * these components are are initialized on demand by
     * explicit call of the initTreeProcessing() 
     */ 
    ClasspathTransformation classpathTransformation;
    ConflictResolver conflictResolver;

    //----------------------------------------------------------------------------
    public MetadataResolutionResult( )
    {
    }
    //----------------------------------------------------------------------------
    public MetadataResolutionResult( MetadataTreeNode root )
    {
        this.treeRoot = root;
    }
    //----------------------------------------------------------------------------
    public MetadataTreeNode getTree()
    {
        return treeRoot;
    }
    //----------------------------------------------------------------------------
    public void setTree( MetadataTreeNode root )
    {
        this.treeRoot = root;
    }
    
    public void initTreeProcessing( PlexusContainer plexus )
    throws ComponentLookupException
    {
        classpathTransformation = (ClasspathTransformation)plexus.lookup(ClasspathTransformation.class);
        conflictResolver = (ConflictResolver)plexus.lookup(ConflictResolver.class);
    }
    //----------------------------------------------------------------------------
    public MetadataGraph getGraph()
    throws MetadataResolutionException
    {
        return treeRoot == null ? null : new MetadataGraph(treeRoot);
    }
    //----------------------------------------------------------------------------
    public MetadataGraph getGraph( ArtifactScopeEnum scope )
    throws MetadataResolutionException, ConflictResolutionException
    {
    	if( treeRoot == null )
    		return null;
    	
    	if( conflictResolver == null )
    		return null;
    	
        return conflictResolver.resolveConflicts( getGraph(), scope );
    }
    //----------------------------------------------------------------------------
    public MetadataGraph getGraph( MetadataResolutionRequestTypeEnum requestType )
    throws MetadataResolutionException, ConflictResolutionException
    {
    	if( requestType == null )
    		return null;
    	
    	if( treeRoot == null )
    		return null;
    	
    	if( conflictResolver == null )
    		return null;
    	
    	if( requestType.equals(MetadataResolutionRequestTypeEnum.classpathCompile) )
    		return conflictResolver.resolveConflicts( getGraph(), ArtifactScopeEnum.compile );
    	else if( requestType.equals(MetadataResolutionRequestTypeEnum.classpathRuntime) )
    		return conflictResolver.resolveConflicts( getGraph(), ArtifactScopeEnum.runtime );
    	else if( requestType.equals(MetadataResolutionRequestTypeEnum.classpathRuntime) )
    		return conflictResolver.resolveConflicts( getGraph(), ArtifactScopeEnum.test );
    	else if( requestType.equals(MetadataResolutionRequestTypeEnum.classpathRuntime) )
    		return conflictResolver.resolveConflicts( getGraph(), ArtifactScopeEnum.test );
    	else if( requestType.equals(MetadataResolutionRequestTypeEnum.graph) )
    		return getGraph();
    	else if( requestType.equals(MetadataResolutionRequestTypeEnum.versionedGraph) ) {
    		return new MetadataGraph( getTree(), true, false );
    	}
    	else if( requestType.equals(MetadataResolutionRequestTypeEnum.scopedGraph) ) {
    		return new MetadataGraph( getTree(), true, true );
    	}
		return null;
    }
    //----------------------------------------------------------------------------
    public ClasspathContainer getClasspath( ArtifactScopeEnum scope )
    throws MetadataGraphTransformationException, MetadataResolutionException
    {
        if( classpathTransformation == null )
        	return null;
        
        MetadataGraph dirtyGraph = getGraph();
        if( dirtyGraph == null )
        	return null;
        
        return classpathTransformation.transform( dirtyGraph, scope, false );
    }
    
    //----------------------------------------------------------------------------
    public MetadataTreeNode getClasspathTree( ArtifactScopeEnum scope )
    throws MetadataGraphTransformationException, MetadataResolutionException
    {
        ClasspathContainer cpc = getClasspath(scope);
        if( cpc == null )
        	return null;
        
        return cpc.getClasspathAsTree();
    }
    //----------------------------------------------------------------------------
    //----------------------------------------------------------------------------
}
