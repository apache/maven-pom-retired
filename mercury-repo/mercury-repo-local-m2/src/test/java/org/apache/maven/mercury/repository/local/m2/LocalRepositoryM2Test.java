package org.apache.maven.mercury.repository.local.m2;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.mercury.artifact.Artifact;
import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;
import org.apache.maven.mercury.repository.api.ArtifactResults;
import org.apache.maven.mercury.repository.api.RepositoryException;

import junit.framework.TestCase;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class LocalRepositoryM2Test
    extends TestCase
{
  File dir;
  LocalRepositoryM2 repo;

  /* (non-Javadoc)
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp()
      throws Exception
  {
    dir = new File("./target/test-classes/repo");
    
    repo = new LocalRepositoryM2( "test", dir );
    
  }
  
  public void testReadTwice()
  throws Exception
  {
    String artifactId = "a:a:4";
    
    ArtifactBasicMetadata bmd = new ArtifactBasicMetadata(artifactId);
    
    ArrayList<ArtifactBasicMetadata> q = new ArrayList<ArtifactBasicMetadata>();
    
    q.add( bmd );
    
    ArtifactResults res = repo.getReader().readArtifacts( q );
    
    assertNotNull( res );
    
   assertFalse( res.hasExceptions() );
   
   assertTrue( res.hasResults() );
   
   List<Artifact> arts = res.getResults( bmd );
   
   assertNotNull( arts );
   
   assertEquals( 1, arts.size() );
   
   Artifact a = arts.get( 0 );
   
   assertNotNull( a );
   
   File f = a.getFile();
   
   assertNotNull( f );
   
   assertTrue( f.exists() );
   
   assertEquals( 14800, f.length() );
   
   // second time
   
   res = repo.getReader().readArtifacts( q );
   
   assertNotNull( res );
   
  assertFalse( res.hasExceptions() );
  
  assertTrue( res.hasResults() );
  
  arts = res.getResults( bmd );
  
  assertNotNull( arts );
  
  assertEquals( 1, arts.size() );
  
  a = arts.get( 0 );
  
  assertNotNull( a );
  
  f = a.getFile();
  
  assertNotNull( f );
  
  assertTrue( f.exists() );
  
  assertEquals( 14800, f.length() );
  
  }

}
