package org.apache.maven.mercury.metadata;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;
import org.apache.maven.mercury.artifact.ArtifactMetadata;
import org.apache.maven.mercury.artifact.ArtifactScopeEnum;
import org.apache.maven.mercury.builder.api.DependencyProcessor;
import org.apache.maven.mercury.repository.api.Repository;
import org.apache.maven.mercury.repository.local.m2.LocalRepositoryM2;
import org.apache.maven.mercury.repository.local.m2.MetadataProcessorMock;


/**
 * 
 * @author Oleg Gusakov
 * @version $Id$
 */
public class DependencyTreeBuilderTest
extends TestCase
{
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger( DependencyTreeBuilderTest.class ); 
  
//  ArtifactMetadata md = new ArtifactMetadata( "pmd:pmd:3.9" );
//  File repo = new File("./target/test-classes/localRepo");

  File repoDir = new File("./target/test-classes/controlledRepo");
  
  DependencyTreeBuilder mt;
  LocalRepositoryM2 localRepo;
  List<Repository> reps;
  DependencyProcessor processor;
  
  //----------------------------------------------------------------------------------------------
  @Override
  protected void setUp()
  throws Exception
  {
    processor = new MetadataProcessorMock();
    localRepo = new LocalRepositoryM2( "local", repoDir );
    
    reps = new ArrayList<Repository>(4);
    reps.add(  localRepo );

    mt = new DependencyTreeBuilder( null, null, null, reps, processor );
  }
  //----------------------------------------------------------------------------------------------
  @Override
  protected void tearDown()
  throws Exception
  {
    super.tearDown();
  }
  //----------------------------------------------------------------------------------------------
  public void testCircularDependency()
  {
    ArtifactMetadata circularMd = new ArtifactMetadata( "a:a:1" );
    try
    {
      mt.buildTree( circularMd );
    }
    catch (MetadataTreeException e)
    {
      assertTrue( "expected circular dependency exception, but got "+e.getClass().getName()
          , e instanceof MetadataTreeCircularDependencyException
      );
      return;
    }
    fail("circular dependency was not detected");
  }
  //----------------------------------------------------------------------------------------------
  public void testBuildTree()
  throws MetadataTreeException
  {
    ArtifactMetadata md = new ArtifactMetadata( "a:a:2" );
    
    MetadataTreeNode root = mt.buildTree( md );
    assertNotNull( "null tree built", root );
    assertEquals( "wrong tree size", 4, root.countNodes() );
  }
  //----------------------------------------------------------------------------------------------
  public void testResolveConflicts()
  throws MetadataTreeException
  {
    ArtifactMetadata md = new ArtifactMetadata( "a:a:2" );
    
    MetadataTreeNode root = mt.buildTree( md );
    assertNotNull( "null tree built", root );
    assertEquals( "wrong tree size", 4, root.countNodes() );

    List<ArtifactMetadata> res = mt.resolveConflicts( ArtifactScopeEnum.compile );
    assertNotNull( "null resolution", res );
    assertEquals( "wrong tree size", 3, res.size() );
    
    assertTrue( "no a:a:2 in the result", assertHasArtifact( res, "a:a:2" ) );
    assertTrue( "no b:b:2 in the result", assertHasArtifact( res, "b:b:2" ) );
    assertTrue( "no c:c:2 in the result", assertHasArtifact( res, "c:c:2" ) );
    
    System.out.println( "testResolveConflicts: " + res );
  }
  //----------------------------------------------------------------------------------------------
  public void testResolveScopedConflicts()
  throws MetadataTreeException
  {
    ArtifactMetadata md = new ArtifactMetadata( "a:a:4" );
    
    MetadataTreeNode root = mt.buildTree( md );
    assertNotNull( "null tree built", root );
    assertEquals( "wrong tree size", 4, root.countNodes() );

    List<ArtifactMetadata> res = mt.resolveConflicts( ArtifactScopeEnum.compile );
    assertNotNull( "null resolution", res );
    assertEquals( "wrong tree size", 2, res.size() );

    System.out.println( "testResolveScopedConflicts: " + res );
    
    assertTrue( "no a:a:4 in the result", assertHasArtifact( res, "a:a:4" ) );
    assertTrue( "no c:c:3 in the result", assertHasArtifact( res, "c:c:3" ) );
    
  }
  //----------------------------------------------------------------------------------------------
  public void testResolveScopedConflictsWithFiltering()
  throws MetadataTreeException
  {
    String title = "testResolveScopedConflictsWithFiltering";
    ArtifactMetadata md = new ArtifactMetadata( "a:a:4" );
    
    List<ArtifactBasicMetadata> exclusions = new ArrayList<ArtifactBasicMetadata>();
    exclusions.add( new ArtifactBasicMetadata("c:c:3") );
    md.setExclusions( exclusions );
    
    MetadataTreeNode root = mt.buildTree( md );
    assertNotNull( "null tree built", root );
    assertEquals( "wrong tree size", 3, root.countNodes() );

    List<ArtifactMetadata> res = mt.resolveConflicts( ArtifactScopeEnum.compile );
    assertNotNull( "null resolution", res );
    assertEquals( "wrong tree size", 2, res.size() );

    System.out.println( title+": " + res );
    
    assertTrue( assertHasArtifact( res, "a:a:4" ) );
    assertFalse( assertHasArtifact( res, "c:c:3" ) );
    
  }
  //----------------------------------------------------------------------------------------------
  public void testResolveScopedConflictsWithFilteringOne()
  throws MetadataTreeException
  {
    String title = "testResolveScopedConflictsWithFilteringOne";
    ArtifactMetadata md = new ArtifactMetadata( "a:a:2" );
    
    List<ArtifactBasicMetadata> exclusions = new ArrayList<ArtifactBasicMetadata>();
    exclusions.add( new ArtifactBasicMetadata("c:c:2") );
    md.setExclusions( exclusions );
    
    MetadataTreeNode root = mt.buildTree( md );
    assertNotNull( "null tree built", root );
    assertEquals( "wrong tree size", 3, root.countNodes() );

    List<ArtifactMetadata> res = mt.resolveConflicts( ArtifactScopeEnum.compile );
    assertNotNull( "null resolution", res );
    assertEquals( "wrong tree size", 2, res.size() );
    
    assertTrue( "no a:a:2 in the result", assertHasArtifact( res, "a:a:2" ) );
    assertTrue( "no b:b:2 in the result", assertHasArtifact( res, "b:b:2" ) );
    assertFalse( "no c:c:2 in the result", assertHasArtifact( res, "c:c:2" ) );
    
    System.out.println( title+": " + res );
    
  }
  //----------------------------------------------------------------------------------------------
  public void testResolveScopedConflictsWithFilteringTwo()
  throws MetadataTreeException
  {
    String title = "testResolveScopedConflictsWithFilteringTwo";
    ArtifactMetadata md = new ArtifactMetadata( "a:a:2" );
    
    List<ArtifactBasicMetadata> exclusions = new ArrayList<ArtifactBasicMetadata>();
    exclusions.add( new ArtifactBasicMetadata("b:b:2") );
    exclusions.add( new ArtifactBasicMetadata("c:c:2") );
    md.setExclusions( exclusions );
    
    MetadataTreeNode root = mt.buildTree( md );
    assertNotNull( "null tree built", root );
    assertEquals( "wrong tree size", 2, root.countNodes() );

    List<ArtifactMetadata> res = mt.resolveConflicts( ArtifactScopeEnum.compile );
    assertNotNull( "null resolution", res );
    assertEquals( "wrong tree size", 2, res.size() );
    
    assertTrue( "no a:a:2 in the result", assertHasArtifact( res, "a:a:2" ) );
    assertTrue( "no b:b:2 in the result", assertHasArtifact( res, "b:b:1" ) );
    assertFalse( "no b:b:2 in the result", assertHasArtifact( res, "b:b:2" ) );
    assertFalse( "no c:c:2 in the result", assertHasArtifact( res, "c:c:2" ) );
    
    System.out.println( title+": " + res );
    
  }
  //----------------------------------------------------------------------------------------------
  public void testResolveScopedConflictsWithFilteringAll()
  throws MetadataTreeException
  {
    String title = "testResolveScopedConflictsWithFilteringTwo";
    ArtifactMetadata md = new ArtifactMetadata( "a:a:2" );
    
    List<ArtifactBasicMetadata> exclusions = new ArrayList<ArtifactBasicMetadata>();
    exclusions.add( new ArtifactBasicMetadata("b:b:1") );
    exclusions.add( new ArtifactBasicMetadata("b:b:2") );
    exclusions.add( new ArtifactBasicMetadata("c:c:2") );
    md.setExclusions( exclusions );
    
    MetadataTreeNode root = mt.buildTree( md );
    assertNotNull( "null tree built", root );
    assertEquals( "wrong tree size", 1, root.countNodes() );

    List<ArtifactMetadata> res = mt.resolveConflicts( ArtifactScopeEnum.compile );
    assertNotNull( "null resolution", res );
    assertEquals( "wrong tree size", 1, res.size() );
    
    assertTrue( "no a:a:2 in the result", assertHasArtifact( res, "a:a:2" ) );
    assertFalse( "no b:b:1 in the result", assertHasArtifact( res, "b:b:1" ) );
    assertFalse( "no b:b:2 in the result", assertHasArtifact( res, "b:b:2" ) );
    assertFalse( "no c:c:2 in the result", assertHasArtifact( res, "c:c:2" ) );
    
    System.out.println( title+": " + res );
    
  }
  //----------------------------------------------------------------------------------------------
  public void testResolveBigConflicts()
  throws MetadataTreeException
  {
    ArtifactMetadata md = new ArtifactMetadata( "a:a:3" );
    
    MetadataTreeNode root = mt.buildTree( md );
    assertNotNull( "null tree built", root );
    assertTrue( "wrong tree size, expected gte 4", 4 <= root.countNodes() );

    List<ArtifactMetadata> res = mt.resolveConflicts( ArtifactScopeEnum.compile );
    assertNotNull( "null resolution", res );

System.out.println("BigRes: "+res);    
    
    assertEquals( "wrong tree size", 3, res.size() );
    
//    assertTrue( "no a:a:2 in the result", assertHasArtifact( res, "a:a:2" ) );
//    assertTrue( "no b:b:1 in the result", assertHasArtifact( res, "b:b:1" ) );
//    assertTrue( "no c:c:2 in the result", assertHasArtifact( res, "c:c:2" ) );
  }
  //----------------------------------------------------------------------------------------------
  private static boolean assertHasArtifact( List<ArtifactMetadata> res, String gav )
  {
    ArtifactMetadata gavMd = new ArtifactMetadata(gav);
    
    for( ArtifactMetadata md : res )
      if( md.sameGAV( gavMd ) )
        return true;
    
    return false;
  }
  //----------------------------------------------------------------------------------------------
  //----------------------------------------------------------------------------------------------
}
