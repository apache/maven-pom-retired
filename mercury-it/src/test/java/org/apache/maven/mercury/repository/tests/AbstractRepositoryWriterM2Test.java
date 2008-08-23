package org.apache.maven.mercury.repository.tests;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

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
import org.apache.maven.mercury.repository.local.m2.LocalRepositoryM2;
import org.apache.maven.mercury.repository.local.m2.MetadataProcessorMock;
import org.apache.maven.mercury.transport.api.Server;
import org.apache.maven.mercury.util.FileUtil;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public abstract class AbstractRepositoryWriterM2Test
extends TestCase
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
  protected static final String secretKeyPass = "testKey82";
  
  Server server;
  HashSet<StreamVerifierFactory> factories;
  
  public void testWriteArtifact()
  throws Exception
  {
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
    da.setStream( getClass().getResourceAsStream( "/maven-core-2.0.9.jar" ) );
    artifacts.add( da );
    
    writer.writeArtifact( artifacts );
    
    assertTrue( new File( targetDirectory, "/org/apache/maven/maven-core/"+repo.getMetadataName()).exists() );
    assertTrue( new File( targetDirectory, "/org/apache/maven/maven-core/"+repo.getMetadataName()+".asc").exists() );
    assertTrue( new File( targetDirectory, "/org/apache/maven/maven-core/"+repo.getMetadataName()+".sha1").exists() );

    assertTrue( new File( targetDirectory, "/org/apache/maven/maven-core/2.0.9/"+repo.getMetadataName()).exists() );
    assertTrue( new File( targetDirectory, "/org/apache/maven/maven-core/2.0.9/"+repo.getMetadataName()+".asc").exists() );
    assertTrue( new File( targetDirectory, "/org/apache/maven/maven-core/2.0.9/"+repo.getMetadataName()+".sha1").exists() );

    assertTrue( af.exists() );
    assertEquals( 159630, af.length() );
    assertTrue( new File( targetDirectory, "/org/apache/maven/maven-core/2.0.9/maven-core-2.0.9.jar.asc").exists() );
    assertTrue( new File( targetDirectory, "/org/apache/maven/maven-core/2.0.9/maven-core-2.0.9.jar.sha1").exists() );
    
    assertTrue( ap.exists() );
    assertEquals( 7785, ap.length() );  
    assertTrue( new File( targetDirectory, "/org/apache/maven/maven-core/2.0.9/maven-core-2.0.9.pom.asc").exists() );
    assertTrue( new File( targetDirectory, "/org/apache/maven/maven-core/2.0.9/maven-core-2.0.9.pom.sha1").exists() );
  }
  
  public void testWriteSnapshotAsTS()
  throws Exception
  {
    Set<Artifact> artifacts = new HashSet<Artifact>(3);
    DefaultArtifact da = new DefaultArtifact( new ArtifactBasicMetadata("org.apache.maven:maven-core:2.0.9-20080805.215925-8") );
    da.setPomBlob( FileUtil.readRawData( getClass().getResourceAsStream( "/maven-core-2.0.9.pom" ) ) );
    da.setStream( getClass().getResourceAsStream( "/maven-core-2.0.9.jar" ) );
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
    Set<Artifact> set = new HashSet<Artifact>(3);
    DefaultArtifact da = new DefaultArtifact( new ArtifactBasicMetadata("org.apache.maven:maven-core:2.0.9-SNAPSHOT") );
    da.setPomBlob( FileUtil.readRawData( getClass().getResourceAsStream( "/maven-core-2.0.9.pom" ) ) );
    da.setStream( getClass().getResourceAsStream( "/maven-core-2.0.9.jar" ) );
    set.add( da );
    
    writer.writeArtifact( set );
    
    File af = new File( targetDirectory, "/org/apache/maven/maven-core/2.0.9-SNAPSHOT/maven-core-2.0.9-SNAPSHOT.jar");
    assertTrue( af.exists() );
    assertEquals( 159630, af.length() );
    
    File ap = new File( targetDirectory, "/org/apache/maven/maven-core/2.0.9-SNAPSHOT/maven-core-2.0.9-SNAPSHOT.pom");
    assertTrue( ap.exists() );
    assertEquals( 7785, ap.length() );  
  }
  
  public void ntestTemp()
  throws Exception
  {
    Set<Artifact> set = new HashSet<Artifact>(3);
    DefaultArtifact da = new DefaultArtifact( new ArtifactBasicMetadata("a:a:4") );
    da.setPomBlob( FileUtil.readRawData( getClass().getResourceAsStream( "/a-4.pom" ) ) );
    da.setStream( getClass().getResourceAsStream( "/a-4.jar" ) );
    set.add( da );
    
    writer.writeArtifact( set );
  }
  
}
