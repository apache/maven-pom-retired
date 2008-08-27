package org.apache.maven.mercury.repository.tests;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.maven.mercury.artifact.Artifact;
import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;
import org.apache.maven.mercury.artifact.ArtifactMetadata;
import org.apache.maven.mercury.artifact.DefaultArtifact;
import org.apache.maven.mercury.artifact.QualityRange;
import org.apache.maven.mercury.builder.api.MetadataProcessor;
import org.apache.maven.mercury.crypto.api.StreamVerifierAttributes;
import org.apache.maven.mercury.crypto.api.StreamVerifierException;
import org.apache.maven.mercury.crypto.api.StreamVerifierFactory;
import org.apache.maven.mercury.crypto.pgp.PgpStreamVerifierFactory;
import org.apache.maven.mercury.crypto.sha.SHA1VerifierFactory;
import org.apache.maven.mercury.repository.api.ArtifactBasicResults;
import org.apache.maven.mercury.repository.api.ArtifactResults;
import org.apache.maven.mercury.repository.api.Repository;
import org.apache.maven.mercury.repository.api.RepositoryException;
import org.apache.maven.mercury.repository.api.AbstractRepOpResult;
import org.apache.maven.mercury.repository.api.RepositoryReader;
import org.apache.maven.mercury.repository.local.m2.LocalRepositoryM2;
import org.apache.maven.mercury.repository.local.m2.MetadataProcessorMock;
import org.apache.maven.mercury.transport.api.Server;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public abstract class AbstractRepositoryReaderM2Test
extends TestCase
{
  Repository repo;
  MetadataProcessor mdProcessor;
  RepositoryReader reader;
  List<ArtifactBasicMetadata> query;
  
  ArtifactBasicMetadata bmd;
  
  private static final String publicKeyFile = "/pgp/pubring.gpg";
  private static final String secretKeyFile = "/pgp/secring.gpg";
  
  Server server;
  HashSet<StreamVerifierFactory> factories;
  //------------------------------------------------------------------------------
  public void testReadReleaseVersion()
  throws IllegalArgumentException, RepositoryException
  {
    bmd = new ArtifactBasicMetadata("a:a:[3,3]");
    query.add( bmd );
    
    ArtifactBasicResults res = reader.readVersions( query );
    
    assertNotNull( res );
    assertFalse( res.hasExceptions() );
    assertTrue( res.hasResults() );
    
    assertEquals( 1, res.getResults().size() );
    
    List<ArtifactBasicMetadata> ror = res.getResult( bmd );
    
    assertNotNull( ror );
    
    if( res.hasExceptions() )
      System.out.println( res.getExceptions() );

    assertEquals( 1, ror.size() );
    
//    System.out.println(qr);
  }
  //------------------------------------------------------------------------------
  public void testReadReleaseRange()
  throws IllegalArgumentException, RepositoryException
  {
    repo.setRepositoryQualityRange( QualityRange.RELEASES_ONLY );
  
    bmd = new ArtifactBasicMetadata("a:a:3");
    query.add( bmd );
    
    ArtifactBasicResults res = reader.readVersions( query );
    
    assertNotNull( res );
    
    if( res.hasExceptions() )
      System.out.println( res.getExceptions() );

    assertFalse( res.hasExceptions() );
    assertTrue( res.hasResults() );
    
    assertEquals( 1, res.getResults().size() );
    
    List<ArtifactBasicMetadata> qr = res.getResult( bmd );
    
    assertNotNull( qr );
    assertTrue( qr.size() > 1 );
    
    assertFalse( qr.contains( new ArtifactBasicMetadata("a:a:5-SNAPSHOT") ) );
    
    System.out.println("query "+bmd+"->"+qr);
    
    ArtifactBasicResults depRes = reader.readDependencies( qr );
    
    assertNotNull( depRes );
    assertFalse( depRes.hasExceptions() );
    assertTrue( depRes.hasResults() );
    
    assertTrue( depRes.hasResults( bmd ) );
    
    List<ArtifactBasicMetadata> deps = depRes.getResult( bmd );
    assertNotNull( deps );
    assertFalse( deps.isEmpty() );

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

    ArtifactResults ror = reader.readArtifacts( query );
    
    assertNotNull( ror );
    
    if( ror.hasExceptions() )
      System.out.println( ror.getExceptions() );
    
    assertFalse( ror.hasExceptions() );
    assertTrue( ror.hasResults() );
    
    List<Artifact> res = ror.getResults(bmd);
    
    assertNotNull( res );
    assertEquals( 1, res.size() );
    
    Artifact da = res.get( 0 );
    
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

    ArtifactResults ror = reader.readArtifacts( query );
    
    assertNotNull( ror );
    
    if( ror.hasExceptions() )
      System.out.println( ror.getExceptions() );
    
    assertFalse( ror.hasExceptions() );
    assertTrue( ror.hasResults() );
    
    List<Artifact> res = ror.getResults(bmd);
    
    assertNotNull( res );
    assertEquals( 1, res.size() );
    
    Artifact da = res.get( 0 );
    
    assertNotNull( da );
    assertNotNull( da.getFile() );
    assertTrue( da.getFile().exists() );
    assertEquals( 159630, da.getFile().length() );
    assertNotNull( da.getPomBlob() );
  }
  //------------------------------------------------------------------------------
  public void testReadSnapshotTS()
  throws IllegalArgumentException, RepositoryException
  {
    bmd = new ArtifactBasicMetadata("a:a:5-20080807.234713-11");
    query.add( bmd );

    ArtifactResults ror = reader.readArtifacts( query );
    
    assertNotNull( ror );
    
    if( ror.hasExceptions() )
      System.out.println( ror.getExceptions() );
    
    assertFalse( ror.hasExceptions() );
    assertTrue( ror.hasResults() );
    
    List<Artifact> res = ror.getResults(bmd);
    
    assertNotNull( res );
    assertEquals( 1, res.size() );
    
    Artifact da = res.get( 0 );
    
    assertNotNull( da );
    assertNotNull( da.getFile() );
    assertTrue( da.getFile().exists() );
    assertEquals( 14800, da.getFile().length() );
    assertNotNull( da.getPomBlob() );
  }
  //------------------------------------------------------------------------------
  public void testReadLatest()
  throws IllegalArgumentException, RepositoryException
  {
    bmd = new ArtifactBasicMetadata("a:a:LATEST");
    query.add( bmd );

    ArtifactResults ror = reader.readArtifacts( query );
    
    assertNotNull( ror );
    
    if( ror.hasExceptions() )
      System.out.println( ror.getExceptions() );
    
    assertFalse( ror.hasExceptions() );
    assertTrue( ror.hasResults() );
    
    List<Artifact> res = ror.getResults(bmd);
    
    assertNotNull( res );
    assertEquals( 1, res.size() );
    
    Artifact da = res.get( 0 );
    
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

    ArtifactResults ror = reader.readArtifacts( query );
    
    assertNotNull( ror );
    
    if( ror.hasExceptions() )
      System.out.println( ror.getExceptions() );
    
    assertFalse( ror.hasExceptions() );
    assertTrue( ror.hasResults() );
    
    List<Artifact> res = ror.getResults(bmd);
    
    assertNotNull( res );
    assertEquals( 1, res.size() );
    
    Artifact da = res.get( 0 );
    
    assertNotNull( da );
    assertEquals( "4", da.getVersion() );
    
    assertNotNull( da.getFile() );
    assertTrue( da.getFile().exists() );
    assertEquals( 14800, da.getFile().length() );
    assertNotNull( da.getPomBlob() );
  }
  //------------------------------------------------------------------------------
  public void testReadAndVerifyGoodArtifact()
  throws IllegalArgumentException, RepositoryException, StreamVerifierException
  {
    // verifiers
    factories = new HashSet<StreamVerifierFactory>();       
    factories.add( 
        new PgpStreamVerifierFactory(
                new StreamVerifierAttributes( PgpStreamVerifierFactory.DEFAULT_EXTENSION, false, true )
                , getClass().getResourceAsStream( publicKeyFile )
                                    )
                  );
    factories.add( new SHA1VerifierFactory( true, false ) );
    server.setReaderStreamVerifierFactories(factories);

    bmd = new ArtifactBasicMetadata("a:a:4");
    query.add( bmd );

    ArtifactResults ror = reader.readArtifacts( query );
    
    assertNotNull( ror );
    
    if( ror.hasExceptions() )
      System.out.println( ror.getExceptions() );
    
    assertFalse( ror.hasExceptions() );
    assertTrue( ror.hasResults() );
    
    List<Artifact> res = ror.getResults(bmd);
    
    assertNotNull( res );
    assertEquals( 1, res.size() );
    
    Artifact da = res.get( 0 );
    
    assertNotNull( da );
    assertNotNull( da.getFile() );
    assertTrue( da.getFile().exists() );
    assertNotNull( da.getPomBlob() );
  }
  //------------------------------------------------------------------------------
  public void testReadAndVerifyArtifactNoSig()
  throws IllegalArgumentException, StreamVerifierException
  {
    // verifiers
    factories = new HashSet<StreamVerifierFactory>();       
    factories.add( 
        new PgpStreamVerifierFactory(
                new StreamVerifierAttributes( PgpStreamVerifierFactory.DEFAULT_EXTENSION, false, true )
                , getClass().getResourceAsStream( publicKeyFile )
                                    )
                  );
    server.setReaderStreamVerifierFactories(factories);

    bmd = new ArtifactBasicMetadata("a:a:3");
    query.add( bmd );

    ArtifactResults ror = null;
    try
    {
      ror = reader.readArtifacts( query );
    }
    catch( RepositoryException e )
    {
      System.out.println( "Expected exception: "+e.getMessage() );
      return;
    }
    assertNotNull( ror );
    if( !ror.hasExceptions() )
      fail( "Artifact a:a:3 does not have .asc signature, PGP verifier is not lenient, but this did not cause a RepositoryException" );

    System.out.println("Expected Exceptions: "+ror.getExceptions() );
  }
  //------------------------------------------------------------------------------
  public void testReadAndVerifyArtifactBadSig()
  throws IllegalArgumentException, StreamVerifierException
  {
    // verifiers
    factories = new HashSet<StreamVerifierFactory>();       
    factories.add( 
        new PgpStreamVerifierFactory(
                new StreamVerifierAttributes( PgpStreamVerifierFactory.DEFAULT_EXTENSION, false, true )
                , getClass().getResourceAsStream( publicKeyFile )
                                    )
                  );
    server.setReaderStreamVerifierFactories(factories);

    bmd = new ArtifactBasicMetadata("a:a:2");
    query.add( bmd );

    ArtifactResults ror = null;
    try
    {
      ror = reader.readArtifacts( query );
    }
    catch( RepositoryException e )
    {
      System.out.println( "Expected exception: "+e.getMessage() );
      return;
    }
    assertNotNull( ror );
    if( !ror.hasExceptions() )
      fail( "Artifact a:a:2 does have a bad .asc (PGP) signature, PGP verifier is not lenient, but this did not cause a RepositoryException" );

    System.out.println("Expected Exceptions: "+ror.getExceptions() );
  }
  //------------------------------------------------------------------------------
  public void testReadAndVerifyArtifactNoSigLenientVerifier()
  throws IllegalArgumentException, StreamVerifierException
  {
    // verifiers
    factories = new HashSet<StreamVerifierFactory>();       
    factories.add( 
        new PgpStreamVerifierFactory(
                new StreamVerifierAttributes( PgpStreamVerifierFactory.DEFAULT_EXTENSION, true, true )
                , getClass().getResourceAsStream( publicKeyFile )
                                    )
                  );
    factories.add( new SHA1VerifierFactory(true,false) );
    server.setReaderStreamVerifierFactories(factories);

    bmd = new ArtifactBasicMetadata("a:a:3");
    query.add( bmd );

    ArtifactResults ror = null;
    try
    {
      ror = reader.readArtifacts( query );
    }
    catch( RepositoryException e )
    {
      fail( "Artifact a:a:3 does not have .asc signature, PGP verifier is lenient, but still caused a RepositoryException: "+e.getMessage() );
    }
    if( ror.hasExceptions() )
      fail( "Artifact a:a:3 does not have .asc signature, PGP verifier is lenient, but still caused exceptions: "+ror.getExceptions() );
  }
  //------------------------------------------------------------------------------
  //------------------------------------------------------------------------------
}
