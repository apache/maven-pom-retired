package org.apache.maven.mercury.repository.local.m2;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;
import org.apache.maven.mercury.artifact.ArtifactMetadata;
import org.apache.maven.mercury.builder.api.MetadataProcessor;
import org.apache.maven.mercury.repository.api.RepositoryException;
import org.apache.maven.mercury.repository.api.RepositoryOperationResult;
import org.apache.maven.mercury.repository.api.RepositoryReader;

import junit.framework.TestCase;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class LocalRepositoryReaderM2Test
    extends TestCase
{
  LocalRepositoryM2 repo;
  MetadataProcessor mdProcessor;
  RepositoryReader reader;
  List<ArtifactBasicMetadata> query;
  
  ArtifactBasicMetadata bmd;
  //------------------------------------------------------------------------------
  @Override
  protected void setUp()
      throws Exception
  {
    repo = new LocalRepositoryM2( "local-repo-m2", new File("./target/test-classes/repo") );
    mdProcessor = new MetadataProcessorMock();
    reader = repo.getReader( mdProcessor );
    
    query = new ArrayList<ArtifactBasicMetadata>();
  }
  //------------------------------------------------------------------------------
  public void testReadReleaseVersion()
  throws IllegalArgumentException, RepositoryException
  {
    bmd = new ArtifactBasicMetadata("a:a:[3,3]");
    query.add( bmd );
    Map<ArtifactBasicMetadata, RepositoryOperationResult<ArtifactBasicMetadata>> 
            res = reader.readVersions( query );
    
    assertNotNull( res );
    assertEquals( 1, res.size() );
    
    RepositoryOperationResult<ArtifactBasicMetadata> ror = res.get( bmd );
    
    assertNotNull( ror );
    assertFalse( ror.hasExceptions() );
    assertTrue( ror.hasResults() );
    
    List<ArtifactBasicMetadata> qr = ror.getResults();
    
    assertNotNull( qr );
    assertEquals( 1, qr.size() );
    
//    System.out.println(qr);
  }
  //------------------------------------------------------------------------------
  public void testReadReleaseRange()
  throws IllegalArgumentException, RepositoryException
  {
    bmd = new ArtifactBasicMetadata("a:a:3");
    query.add( bmd );
    Map<ArtifactBasicMetadata, RepositoryOperationResult<ArtifactBasicMetadata>> res = reader.readVersions( query );
    
    assertNotNull( res );
    assertEquals( 1, res.size() );
    
    RepositoryOperationResult<ArtifactBasicMetadata> ror = res.get( bmd );
    
    assertNotNull( ror );
    assertFalse( ror.hasExceptions() );
    assertTrue( ror.hasResults() );
    
    List<ArtifactBasicMetadata> qr = ror.getResults();
    
    assertNotNull( qr );
    assertTrue( qr.size() > 1 );
    
//    assertFalse( qr.contains( new ArtifactBasicMetadata("a:a:5-SNAPSHOT") ) );
    
    System.out.println("query "+bmd+"->"+qr);
    
    Map<ArtifactBasicMetadata,ArtifactMetadata> depRes = reader.readDependencies( qr );
    
    assertNotNull( depRes );
    assertTrue( depRes.size() > 1 );
    assertTrue( depRes.containsKey( bmd ) );
    
    ArtifactMetadata amd = depRes.get( bmd );
    
    List<ArtifactBasicMetadata> deps = amd.getDependencies();
    assertNotNull( deps );

//    System.out.println(deps);

    assertTrue( deps.contains( new ArtifactBasicMetadata("b:b:2") ) );
    assertTrue( deps.contains( new ArtifactBasicMetadata("c:c:(1,)") ) );
    
  }
  //------------------------------------------------------------------------------
  public void testReadLatest()
  {
    // TODO
  }
  //------------------------------------------------------------------------------
  public void testReadSnapshot()
  {
    // TODO
  }
  //------------------------------------------------------------------------------
  public void testReadSnapshotTimeStamp()
  {
    // TODO
  }
  //------------------------------------------------------------------------------
  //------------------------------------------------------------------------------
}
