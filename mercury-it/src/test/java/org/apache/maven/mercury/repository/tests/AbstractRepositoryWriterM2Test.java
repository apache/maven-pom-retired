package org.apache.maven.mercury.repository.tests;

import java.io.File;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.apache.maven.mercury.artifact.Artifact;
import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;
import org.apache.maven.mercury.artifact.DefaultArtifact;
import org.apache.maven.mercury.builder.api.MetadataProcessor;
import org.apache.maven.mercury.crypto.api.StreamVerifierAttributes;
import org.apache.maven.mercury.crypto.api.StreamVerifierFactory;
import org.apache.maven.mercury.crypto.pgp.PgpStreamVerifierFactory;
import org.apache.maven.mercury.crypto.sha.SHA1VerifierFactory;
import org.apache.maven.mercury.repository.api.Repository;
import org.apache.maven.mercury.repository.api.RepositoryReader;
import org.apache.maven.mercury.repository.api.RepositoryWriter;
import org.apache.maven.mercury.transport.api.Server;
import org.apache.maven.mercury.util.FileUtil;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.appbooter.ForkedAppBooter;
import org.sonatype.nexus.client.NexusClient;
import org.sonatype.nexus.client.rest.NexusRestClient;
import org.sonatype.nexus.rest.model.RepositoryListResource;

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
  
  MetadataProcessor mdProcessor;
  
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
    
  }
  
  
  @Override
  protected void tearDown()
  throws Exception
  {
    super.tearDown();
//    if( nexusForkedAppBooter != null )
//    {
//      nexusForkedAppBooter.stop();
//      nexusForkedAppBooter = null;
//    }
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
    
    da.setPomBlob( FileUtil.readRawData( getClass().getResourceAsStream( "/maven-core-2.0.9.pom" ) ) );
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
    assertEquals( 159630, af.length() );
    assertTrue( new File( targetDirectory, "/org/apache/maven/maven-core/2.0.9/maven-core-2.0.9.jar.asc").exists() );
    FileUtil.verify( af, vFacPgp, false, true );
    assertTrue( new File( targetDirectory, "/org/apache/maven/maven-core/2.0.9/maven-core-2.0.9.jar.sha1").exists() );
    FileUtil.verify( af, vFacSha1, false, true );
    
    assertTrue( ap.exists() );
    assertEquals( 7785, ap.length() );  
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
    da.setPomBlob( FileUtil.readRawData( getClass().getResourceAsStream( "/maven-core-2.0.9.pom" ) ) );
    da.setFile( artifactBinary );
    artifacts.add( da );
    
    writer.writeArtifact( artifacts );
    
    File af = new File( targetDirectory, "/org/apache/maven/maven-core/2.0.9-SNAPSHOT/maven-core-2.0.9-20080805.215925-8.jar");
    assertTrue( af.exists() );
    assertEquals( 159630, af.length() );
    
    File ap = new File( targetDirectory, "/org/apache/maven/maven-core/2.0.9-SNAPSHOT/maven-core-2.0.9-20080805.215925-8.pom");
    assertTrue( ap.exists() );
    assertEquals( 7785, ap.length() );  
  }
  
  public void testWriteSnapshot()
  throws Exception
  {
    setSnapshots();
    
    Set<Artifact> set = new HashSet<Artifact>(3);
    DefaultArtifact da = new DefaultArtifact( new ArtifactBasicMetadata("org.apache.maven:maven-core:2.0.9-SNAPSHOT") );
    da.setPomBlob( FileUtil.readRawData( getClass().getResourceAsStream( "/maven-core-2.0.9.pom" ) ) );
    da.setFile( artifactBinary );
    set.add( da );
    
    writer.writeArtifact( set );
    
    File af = new File( targetDirectory, "/org/apache/maven/maven-core/2.0.9-SNAPSHOT/maven-core-2.0.9-SNAPSHOT.jar");
    assertTrue( af.exists() );
    assertEquals( 159630, af.length() );
    
    File ap = new File( targetDirectory, "/org/apache/maven/maven-core/2.0.9-SNAPSHOT/maven-core-2.0.9-SNAPSHOT.pom");
    assertTrue( ap.exists() );
    assertEquals( 7785, ap.length() );  
  }
  
}
