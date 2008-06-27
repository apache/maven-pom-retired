package org.apache.maven.mercury.metadata;

import java.io.File;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.LogFactoryImpl;
import org.apache.maven.mercury.repository.DefaultLocalRepository;


/**
 * 
 * @author Oleg Gusakov
 * @version $Id$
 */
public class MetadataTreeTest
extends TestCase
{
  private static final Log log = LogFactoryImpl.getLog( MetadataTreeTest.class );
  
//  ArtifactMetadata md = new ArtifactMetadata( "pmd:pmd:3.9" );
//  File repo = new File("./target/test-classes/localRepo");

  ArtifactMetadata a1 = new ArtifactMetadata( "a:a:1" );
  ArtifactMetadata md = new ArtifactMetadata( "a:a:2" );
  File repo = new File("./target/test-classes/controlledRepo");
  
  MetadataTree mt;
  MockMetadataSource mms = new MockMetadataSource();

  @Override
  protected void setUp()
  throws Exception
  {
System.out.println("Current dir is "+ new File(".").getCanonicalPath() );
    mms = new MockMetadataSource();
    
    mt = new MetadataTree( 
          mms
        , null
        , null
        , new DefaultLocalRepository( "local", null, repo )
        , null
                          );
  }

  @Override
  protected void tearDown() throws Exception
  {
    super.tearDown();
  }
  
  public void testCircularDependency()
  {
    
    try
    {
      mt.buildTree( a1 );
    }
    catch (MetadataTreeException e)
    {
      assert e instanceof MetadataTreeCircularDependencyException : "expected circular dependency exception, but got "+e.getClass().getName();
      return;
    }
    fail("circular dependency was not detected");
  }
  
  public void testBuildTree()
  throws MetadataTreeException
  {
    MetadataTreeNode root = mt.buildTree( md );
    
    System.out.println( "got tree of "+root.countNodes() );
  }
  
  public void testResolveConflicts()
  throws MetadataTreeException
  {
    MetadataTreeNode root = mt.buildTree( md );
    List<ArtifactMetadata> res = mt.resolveConflicts(root);
    
    System.out.println( "No Conflict: " + res );
  }

}
