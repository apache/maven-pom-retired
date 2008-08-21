package org.apache.maven.mercury.repository.local.m2;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;
import org.apache.maven.mercury.artifact.ArtifactMetadata;
import org.apache.maven.mercury.artifact.DefaultArtifact;
import org.apache.maven.mercury.artifact.QualityRange;
import org.apache.maven.mercury.builder.api.MetadataProcessor;
import org.apache.maven.mercury.repository.api.RepositoryException;
import org.apache.maven.mercury.repository.api.RepositoryOperationResult;
import org.apache.maven.mercury.repository.api.RepositoryReader;
import org.apache.maven.mercury.repository.api.RepositoryWriter;
import org.apache.maven.mercury.util.FileUtil;

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
    repo.setRepositoryQualityRange( QualityRange.RELEASES_ONLY );
  
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
    
    assertFalse( qr.contains( new ArtifactBasicMetadata("a:a:5-SNAPSHOT") ) );
    
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
  public void testReadArtifacts()
  throws IllegalArgumentException, RepositoryException
  {
    bmd = new ArtifactBasicMetadata("a:a:3");
    query.add( bmd );

    RepositoryOperationResult<DefaultArtifact> ror = reader.readArtifacts( query );
    
    assertNotNull( ror );
    assertFalse( ror.hasExceptions() );
    assertTrue( ror.hasResults() );
    
    List<DefaultArtifact> res = ror.getResults();
    
    assertNotNull( res );
    assertEquals( 1, res.size() );
    
    DefaultArtifact da = res.get( 0 );
    
    assertNotNull( da );
    assertNotNull( da.getFile() );
    assertTrue( da.getFile().exists() );
    assertNotNull( da.getPomBlob() );
  }
  //------------------------------------------------------------------------------
  public void testReadSnapshot()
  throws IllegalArgumentException, RepositoryException
  {
    bmd = new ArtifactBasicMetadata("a:a:5-SNAPSHOT");
    query.add( bmd );

    RepositoryOperationResult<DefaultArtifact> ror = reader.readArtifacts( query );
    
    assertNotNull( ror );
    assertFalse( ror.hasExceptions() );
    assertTrue( ror.hasResults() );
    
    List<DefaultArtifact> res = ror.getResults();
    
    assertNotNull( res );
    assertEquals( 1, res.size() );
    
    DefaultArtifact da = res.get( 0 );
    
    assertNotNull( da );
    assertNotNull( da.getFile() );
    assertTrue( da.getFile().exists() );
    assertEquals( 159630, da.getFile().length() );
    assertNotNull( da.getPomBlob() );
    
System.out.println( new String(da.getPomBlob()) );
  }
  //------------------------------------------------------------------------------
  public void testReadSnapshotTS()
  throws IllegalArgumentException, RepositoryException
  {
    bmd = new ArtifactBasicMetadata("a:a:5-20080807.234713-11");
    query.add( bmd );

    RepositoryOperationResult<DefaultArtifact> ror = reader.readArtifacts( query );
    
    assertNotNull( ror );
    assertFalse( ror.hasExceptions() );
    assertTrue( ror.hasResults() );
    
    List<DefaultArtifact> res = ror.getResults();
    
    assertNotNull( res );
    assertEquals( 1, res.size() );
    
    DefaultArtifact da = res.get( 0 );
    
    assertNotNull( da );
    assertNotNull( da.getFile() );
    assertTrue( da.getFile().exists() );
    assertEquals( 14800, da.getFile().length() );
    assertNotNull( da.getPomBlob() );
    
System.out.println( new String(da.getPomBlob()) );
  }
  //------------------------------------------------------------------------------
  public void testReadLatest()
  throws IllegalArgumentException, RepositoryException
  {
    bmd = new ArtifactBasicMetadata("a:a:LATEST");
    query.add( bmd );

    RepositoryOperationResult<DefaultArtifact> ror = reader.readArtifacts( query );
    
    assertNotNull( ror );
    assertFalse( ror.hasExceptions() );
    assertTrue( ror.hasResults() );
    
    List<DefaultArtifact> res = ror.getResults();
    
    assertNotNull( res );
    assertEquals( 1, res.size() );
    
    DefaultArtifact da = res.get( 0 );
    
    assertNotNull( da );
    assertEquals( "5-SNAPSHOT", da.getVersion() );
    
    assertNotNull( da.getFile() );
    assertTrue( da.getFile().exists() );
    assertEquals( 159630, da.getFile().length() );
    assertNotNull( da.getPomBlob() );
    
  }
  //------------------------------------------------------------------------------
  public void testReadRelease()
  throws IllegalArgumentException, RepositoryException
  {
    bmd = new ArtifactBasicMetadata("a:a:RELEASE");
    query.add( bmd );

    RepositoryOperationResult<DefaultArtifact> ror = reader.readArtifacts( query );
    
    assertNotNull( ror );
    assertFalse( ror.hasExceptions() );
    assertTrue( ror.hasResults() );
    
    List<DefaultArtifact> res = ror.getResults();
    
    assertNotNull( res );
    assertEquals( 1, res.size() );
    
    DefaultArtifact da = res.get( 0 );
    
    assertNotNull( da );
    assertEquals( "4", da.getVersion() );
    
    assertNotNull( da.getFile() );
    assertTrue( da.getFile().exists() );
    assertEquals( 14800, da.getFile().length() );
    assertNotNull( da.getPomBlob() );
    
System.out.println( new String(da.getPomBlob()) );
  }
  //------------------------------------------------------------------------------
  //------------------------------------------------------------------------------
}
