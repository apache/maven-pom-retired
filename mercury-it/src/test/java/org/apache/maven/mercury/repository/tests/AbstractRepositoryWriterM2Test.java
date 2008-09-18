package org.apache.maven.mercury.repository.tests;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.mercury.artifact.Artifact;
import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;
import org.apache.maven.mercury.artifact.DefaultArtifact;
import org.apache.maven.mercury.builder.api.DependencyProcessor;
import org.apache.maven.mercury.crypto.api.StreamVerifierAttributes;
import org.apache.maven.mercury.crypto.api.StreamVerifierFactory;
import org.apache.maven.mercury.crypto.pgp.PgpStreamVerifierFactory;
import org.apache.maven.mercury.crypto.sha.SHA1VerifierFactory;
import org.apache.maven.mercury.repository.api.Repository;
import org.apache.maven.mercury.repository.api.RepositoryReader;
import org.apache.maven.mercury.repository.api.RepositoryWriter;
import org.apache.maven.mercury.repository.metadata.Metadata;
import org.apache.maven.mercury.repository.metadata.MetadataBuilder;
import org.apache.maven.mercury.transport.api.Server;
import org.apache.maven.mercury.util.FileUtil;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusTestCase;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public abstract class AbstractRepositoryWriterM2Test
extends PlexusTestCase
{
  Repository repo;
  
  File targetDirectory; 
  
  DependencyProcessor mdProcessor;
  
  RepositoryReader reader;
  RepositoryWriter writer;

  List<ArtifactBasicMetadata> query;
  
  ArtifactBasicMetadata bmd;
  
  String pomBlob;
  
  protected static final String keyId   = "0EDB5D91141BC4F2";

  protected static final String secretKeyFile = "/pgp/secring.gpg";
  protected static final String publicKeyFile = "/pgp/pubring.gpg";
  protected static final String secretKeyPass = "testKey82";
  
  PgpStreamVerifierFactory pgpF;
  SHA1VerifierFactory      sha1F;
  HashSet<StreamVerifierFactory> vFacPgp;
  HashSet<StreamVerifierFactory> vFacSha1;
  
  Server server;
  HashSet<StreamVerifierFactory> factories;
  
  File f;
  
  File artifactBinary;
  long binarySize = -1L;

  File artifactPom;
  long pomSize = -1L;
  byte [] pomBytes;

  static PlexusContainer plexus;
  
  /** current test works with snapshots 
   * @throws Exception */
  abstract void setReleases() throws Exception;
  /** current test works with releases */
  abstract void setSnapshots() throws Exception;
  
  
  @Override
  protected void setUp()
  throws Exception
  {
    super.setUp();

    if( plexus == null )
      plexus = getContainer();

    pgpF = new PgpStreamVerifierFactory(
        new StreamVerifierAttributes( PgpStreamVerifierFactory.DEFAULT_EXTENSION, false, true )
        , getClass().getResourceAsStream( publicKeyFile )
                            );
    sha1F = new SHA1VerifierFactory( false, false );

    vFacPgp  = new HashSet<StreamVerifierFactory>(1);
    vFacSha1 = new HashSet<StreamVerifierFactory>(1);

    vFacPgp.add( pgpF );
    vFacSha1.add( sha1F );

    artifactBinary = File.createTempFile( "test-repo-writer", "bin" );
    FileUtil.writeRawData( artifactBinary, getClass().getResourceAsStream( "/maven-core-2.0.9.jar" ) );
    binarySize = artifactBinary.length();

    artifactPom = File.createTempFile( "test-repo-writer", "pom" );
    FileUtil.writeRawData( artifactPom, getClass().getResourceAsStream( "/maven-core-2.0.9.pom" ) );
    pomSize = artifactPom.length();
    pomBytes = FileUtil.readRawData( artifactPom );
  }
  
  
  @Override
  protected void tearDown()
  throws Exception
  {
    super.tearDown();
  }

  public void testWriteArtifact()
  throws Exception
  {
    setReleases();
    
    File af = new File( targetDirectory, "/org/apache/maven/maven-core/2.0.9/maven-core-2.0.9.jar");

    assertFalse( af.exists() );
    assertFalse( new File( targetDirectory, "/org/apache/maven/maven-core/2.0.9/maven-core-2.0.9.jar.asc").exists() );
    assertFalse( new File( targetDirectory, "/org/apache/maven/maven-core/2.0.9/maven-core-2.0.9.jar.sha1").exists() );
    
    File ap = new File( targetDirectory, "/org/apache/maven/maven-core/2.0.9/maven-core-2.0.9.pom");
    assertFalse( ap.exists() );
    assertFalse( new File( targetDirectory, "/org/apache/maven/maven-core/2.0.9/maven-core-2.0.9.pom.asc").exists() );
    assertFalse( new File( targetDirectory, "/org/apache/maven/maven-core/2.0.9/maven-core-2.0.9.pom.sha1").exists() );
    
    assertFalse( new File( targetDirectory, "/org/apache/maven/maven-core/"+repo.getMetadataName()).exists() );
    assertFalse( new File( targetDirectory, "/org/apache/maven/maven-core/"+repo.getMetadataName()+".asc").exists() );
    assertFalse( new File( targetDirectory, "/org/apache/maven/maven-core/"+repo.getMetadataName()+".sha1").exists() );

    assertFalse( new File( targetDirectory, "/org/apache/maven/maven-core/2.0.9/"+repo.getMetadataName()).exists() );
    assertFalse( new File( targetDirectory, "/org/apache/maven/maven-core/2.0.9/"+repo.getMetadataName()+".asc").exists() );
    assertFalse( new File( targetDirectory, "/org/apache/maven/maven-core/2.0.9/"+repo.getMetadataName()+".sha1").exists() );

    Set<Artifact> artifacts = new HashSet<Artifact>(3);
    DefaultArtifact da = new DefaultArtifact( new ArtifactBasicMetadata("org.apache.maven:maven-core:2.0.9") );
    
    da.setPomBlob( pomBytes );
    da.setFile( artifactBinary );
    artifacts.add( da );
    
    writer.writeArtifact( artifacts );
    
    f = new File( targetDirectory, "/org/apache/maven/maven-core/"+repo.getMetadataName());
    assertTrue( f.exists() );
    assertTrue( new File( targetDirectory, "/org/apache/maven/maven-core/"+repo.getMetadataName()+".asc").exists() );
    FileUtil.verify( f, vFacPgp, false, true );
    assertTrue( new File( targetDirectory, "/org/apache/maven/maven-core/"+repo.getMetadataName()+".sha1").exists() );
    FileUtil.verify( f, vFacSha1, false, true );

    f = new File( targetDirectory, "/org/apache/maven/maven-core/2.0.9/"+repo.getMetadataName());
    assertTrue( f.exists() );
    assertTrue( new File( targetDirectory, "/org/apache/maven/maven-core/2.0.9/"+repo.getMetadataName()+".asc").exists() );
    FileUtil.verify( f, vFacPgp, false, true );
    assertTrue( new File( targetDirectory, "/org/apache/maven/maven-core/2.0.9/"+repo.getMetadataName()+".sha1").exists() );
    FileUtil.verify( f, vFacSha1, false, true );
    
    assertTrue( af.exists() );
    assertEquals( binarySize, af.length() );
    assertTrue( new File( targetDirectory, "/org/apache/maven/maven-core/2.0.9/maven-core-2.0.9.jar.asc").exists() );
    FileUtil.verify( af, vFacPgp, false, true );
    assertTrue( new File( targetDirectory, "/org/apache/maven/maven-core/2.0.9/maven-core-2.0.9.jar.sha1").exists() );
    FileUtil.verify( af, vFacSha1, false, true );
    
    assertTrue( ap.exists() );
    assertEquals( pomSize, ap.length() );  
    assertTrue( new File( targetDirectory, "/org/apache/maven/maven-core/2.0.9/maven-core-2.0.9.pom.asc").exists() );
    FileUtil.verify( ap, vFacPgp, false, true );
    assertTrue( new File( targetDirectory, "/org/apache/maven/maven-core/2.0.9/maven-core-2.0.9.pom.sha1").exists() );
    FileUtil.verify( ap, vFacSha1, false, true );
  }
  
  public void testWriteSnapshotAsTS()
  throws Exception
  {
    setSnapshots();
    
    Set<Artifact> artifacts = new HashSet<Artifact>(3);
    DefaultArtifact da = new DefaultArtifact( new ArtifactBasicMetadata("org.apache.maven:maven-core:2.0.9-20080805.215925-8") );
    da.setPomBlob( pomBytes );
    da.setFile( artifactBinary );
    artifacts.add( da );
    
    writer.writeArtifact( artifacts );
    
    File af = new File( targetDirectory, "/org/apache/maven/maven-core/2.0.9-SNAPSHOT/maven-core-2.0.9-20080805.215925-8.jar");
    assertTrue( af.exists() );
    assertEquals( binarySize, af.length() );
    
    File ap = new File( targetDirectory, "/org/apache/maven/maven-core/2.0.9-SNAPSHOT/maven-core-2.0.9-20080805.215925-8.pom");
    assertTrue( ap.exists() );
    assertEquals( pomSize, ap.length() );  
  }
  
  public void testWriteSnapshot()
  throws Exception
  {
    setSnapshots();
    
    Set<Artifact> set = new HashSet<Artifact>(3);
    DefaultArtifact da = new DefaultArtifact( new ArtifactBasicMetadata("org.apache.maven:maven-core:2.0.9-SNAPSHOT") );
    da.setPomBlob( pomBytes );
    da.setFile( artifactBinary );
    set.add( da );
    
    writer.writeArtifact( set );
    
    File af = new File( targetDirectory, "/org/apache/maven/maven-core/2.0.9-SNAPSHOT/maven-core-2.0.9-SNAPSHOT.jar");
    assertTrue( af.exists() );
    assertEquals( binarySize, af.length() );
    
    File ap = new File( targetDirectory, "/org/apache/maven/maven-core/2.0.9-SNAPSHOT/maven-core-2.0.9-SNAPSHOT.pom");
    assertTrue( ap.exists() );
    assertEquals( pomSize, ap.length() );  
  }
  
  public void testWriteContentionSingleArtifact()
  throws Exception
  {
    setSnapshots();
    
    Set<Artifact> set = new HashSet<Artifact>(40);

    // prep. artifacts
    for( int i=0; i<20; i++ )
    {
      String si = ""+i;
      
      DefaultArtifact da = new DefaultArtifact( new ArtifactBasicMetadata("org.apache.maven:maven-core:2.0.9-20080805.215925-"+si) );
      da.setPomBlob( pomBytes );
      File ab = File.createTempFile( "test-core-", "-bin" );
      FileUtil.writeRawData( ab, getClass().getResourceAsStream( "/maven-core-2.0.9.jar" ) );
      da.setFile( ab );
      set.add( da );
    }
    
    assertEquals( 20, set.size() );
    
    long start = System.currentTimeMillis();
    // write 'em
    writer.writeArtifact( set );
    
    System.out.println("Took "+(System.currentTimeMillis()-start)+" millis to write "+set.size()+" artifacts");
    System.out.flush();
    
    // check if the showed up in the repo
    for( int i=0; i<20; i++ )
    {
      String si = ""+i;

      String fn = targetDirectory.getAbsolutePath()+"/org/apache/maven/maven-core/2.0.9-SNAPSHOT/maven-core-2.0.9-20080805.215925-"+si+".jar";
      File af = new File( fn );
      assertTrue( fn+" does not exist", af.exists() );
      assertEquals( binarySize, af.length() );
      
      // is pom there also?
      fn = targetDirectory.getAbsolutePath()+"/org/apache/maven/maven-core/2.0.9-SNAPSHOT/maven-core-2.0.9-20080805.215925-"+si+".pom";
      File ap = new File( fn );
      assertTrue( fn+" does not exist", ap.exists() );
      assertEquals( pomSize, ap.length() );
    }
    
    // check GAV metadata has all versions
    String mdFile = targetDirectory.getAbsolutePath()+"/org/apache/maven/maven-core/2.0.9-SNAPSHOT/"+repo.getMetadataName();
    byte [] mdBytes = FileUtil.readRawData( new File(mdFile) );
    Metadata md = MetadataBuilder.read( new ByteArrayInputStream(mdBytes) );
    
    assertNotNull( md );
    assertNotNull( md.getVersioning() );
    assertNotNull( md.getVersioning().getVersions() );
    assertFalse( md.getVersioning().getVersions().isEmpty() );
    
    List<String> versions = md.getVersioning().getVersions();
    System.out.println( versions.size()+" versions: " + versions );

    assertEquals( 20, versions.size() );
    
    for( int i=0; i<20;i++ )
    {
      String v = "2.0.9-20080805.215925-"+i;
      assertTrue( "Did not find "+v+" in GAV metadata "+mdFile+"\n"+new String(mdBytes), versions.contains( v ) );
    }
  }
  
  
  public void testWriteContentionMultipleArtifacts()
  throws Exception
  {
    setSnapshots();
    
    Set<Artifact> set = new HashSet<Artifact>(40);

    // prep. artifacts
    for( int i=0; i<20; i++ )
    {
      String si = ""+i;
      
      DefaultArtifact da = new DefaultArtifact( new ArtifactBasicMetadata("org.apache.maven:maven-core:2.0."+si+"-SNAPSHOT") );
      da.setPomBlob( pomBytes );
      File ab = File.createTempFile( "test-core-", "-bin" );
      FileUtil.writeRawData( ab, getClass().getResourceAsStream( "/maven-core-2.0.9.jar" ) );
      da.setFile( ab );
      set.add( da );

      da = new DefaultArtifact( new ArtifactBasicMetadata("org.apache.maven:maven-mercury:2.0."+si+"-SNAPSHOT") );
      da.setPomBlob( pomBytes );
      ab = File.createTempFile( "test-mercury-", "-bin" );
      FileUtil.writeRawData( ab, getClass().getResourceAsStream( "/maven-core-2.0.9.jar" ) );
      da.setFile( ab );
      set.add( da );
    }
    
    assertEquals( 40, set.size() );
    
    long start = System.currentTimeMillis();
    // write 'em
    writer.writeArtifact( set );
    
    System.out.println("Took "+(System.currentTimeMillis()-start)+" millis to write "+set.size()+" artifacts");
    System.out.flush();
    
    // check if the showed up in the repo
    for( int i=0; i<20; i++ )
    {
      String si = ""+i;

      String fn = targetDirectory.getAbsolutePath()+"/org/apache/maven/maven-core/2.0."+si+"-SNAPSHOT/maven-core-2.0."+si+"-SNAPSHOT.jar";
      File af = new File( targetDirectory, "/org/apache/maven/maven-core/2.0."+si+"-SNAPSHOT/maven-core-2.0."+si+"-SNAPSHOT.jar" );
      assertTrue( fn+" does not exist", af.exists() );
      assertEquals( binarySize, af.length() );
      
      fn = targetDirectory.getAbsolutePath()+"/org/apache/maven/maven-core/2.0."+si+"-SNAPSHOT/maven-core-2.0."+si+"-SNAPSHOT.pom";
      File ap = new File( targetDirectory, "/org/apache/maven/maven-core/2.0."+si+"-SNAPSHOT/maven-core-2.0."+si+"-SNAPSHOT.pom");
      assertTrue( fn+" does not exist", ap.exists() );
      assertEquals( pomSize, ap.length() );

      fn = targetDirectory.getAbsolutePath()+"/org/apache/maven/maven-mercury/2.0."+si+"-SNAPSHOT/maven-mercury-2.0."+i+"-SNAPSHOT.jar";
      af = new File( targetDirectory, "/org/apache/maven/maven-mercury/2.0."+si+"-SNAPSHOT/maven-mercury-2.0."+i+"-SNAPSHOT.jar");
      assertTrue( fn+" does not xist", af.exists() );
      assertEquals( binarySize, af.length() );
      
      fn = targetDirectory.getAbsolutePath()+"/org/apache/maven/maven-mercury/2.0."+si+"-SNAPSHOT/maven-mercury-2.0."+i+"-SNAPSHOT.pom";
      ap = new File( targetDirectory, "/org/apache/maven/maven-mercury/2.0."+si+"-SNAPSHOT/maven-mercury-2.0."+i+"-SNAPSHOT.pom");
      assertTrue( ap.exists() );
      assertEquals( pomSize, ap.length() );
    }
    
    // check GA metadata has all versions
    String mdFile = targetDirectory.getAbsolutePath()+"/org/apache/maven/maven-mercury/"+repo.getMetadataName();
    byte [] mdBytes = FileUtil.readRawData( new File(mdFile) );
    Metadata md = MetadataBuilder.read( new ByteArrayInputStream(mdBytes) );
    
    assertNotNull( md );
    assertNotNull( md.getVersioning() );
    assertNotNull( md.getVersioning().getVersions() );
    assertFalse( md.getVersioning().getVersions().isEmpty() );
    
    List<String> versions = md.getVersioning().getVersions();

    assertEquals( 20, versions.size() );
    
    for( int i=0; i<20;i++ )
    {
      String v = "2.0."+i+"-SNAPSHOT";
      assertTrue( "Did not find "+v+" in GA metadata "+mdFile+"\n"+new String(mdBytes), versions.contains( v ) );
    }
    
    mdFile = targetDirectory.getAbsolutePath()+"/org/apache/maven/maven-core/"+repo.getMetadataName();
    mdBytes = FileUtil.readRawData( new File(mdFile) );
    md = MetadataBuilder.read( new ByteArrayInputStream(mdBytes) );
    
    assertNotNull( md );
    assertNotNull( md.getVersioning() );
    assertNotNull( md.getVersioning().getVersions() );
    assertFalse( md.getVersioning().getVersions().isEmpty() );
    
    versions = md.getVersioning().getVersions();
    
    assertEquals( 20, versions.size() );

    for( int i=0; i<20;i++ )
    {
      String v = "2.0."+i+"-SNAPSHOT";
      assertTrue( "Did not find "+v+" in GA metadata "+mdFile+"\n"+new String(mdBytes), versions.contains( v ) );
    }
  }
  
}
