package org.apache.maven.mercury.repository.local.m2;

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
import org.apache.maven.mercury.repository.api.RepositoryReader;
import org.apache.maven.mercury.repository.api.RepositoryWriter;
import org.apache.maven.mercury.transport.api.Server;
import org.apache.maven.mercury.util.FileUtil;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class LocalRepositoryWriterM2Test
    extends TestCase
{
  LocalRepositoryM2 repo;
  
  MetadataProcessor mdProcessor;
  
  RepositoryReader reader;
  RepositoryWriter writer;

  List<ArtifactBasicMetadata> query;
  
  ArtifactBasicMetadata bmd;
  
  String pomBlob;
  
  private static final String keyId   = "0EDB5D91141BC4F2";

  private static final String secretKeyFile = "/pgp/secring.gpg";
  private static final String secretKeyPass = "testKey82";
  
  Server server;
  HashSet<StreamVerifierFactory> factories;
  //------------------------------------------------------------------------------
  @Override
  protected void setUp()
      throws Exception
  {
    File repoFile = new File("./target/test-classes/tempRepo");
    FileUtil.copy( new File("./target/test-classes/repo"), repoFile, true );
    
    mdProcessor = new MetadataProcessorMock();
    
    query = new ArrayList<ArtifactBasicMetadata>();
    
    server = new Server( "test", repoFile.toURL() );
    factories = new HashSet<StreamVerifierFactory>();       
    factories.add( 
        new PgpStreamVerifierFactory(
                new StreamVerifierAttributes( PgpStreamVerifierFactory.DEFAULT_EXTENSION, false, true )
                , getClass().getResourceAsStream( secretKeyFile )
                , keyId
                , secretKeyPass
                                    )
                  );
    factories.add( new SHA1VerifierFactory(false,false) );
    server.setStreamVerifierFactories(factories);
      
    repo = new LocalRepositoryM2( server );
    reader = repo.getReader( mdProcessor );
    writer = repo.getWriter();
    
      
  }
    
  public void testWriteArtifact()
  throws Exception
  {
    Set<Artifact> set = new HashSet<Artifact>(3);
    DefaultArtifact da = new DefaultArtifact( new ArtifactBasicMetadata("org.apache.maven:maven-core:2.0.9") );
    da.setPomBlob( FileUtil.readRawData( getClass().getResourceAsStream( "/maven-core-2.0.9.pom" ) ) );
    da.setStream( getClass().getResourceAsStream( "/maven-core-2.0.9.jar" ) );
    set.add( da );
    
    writer.writeArtifact( set );
    
    File af = new File( repo.getDirectory(), "/org/apache/maven/maven-core/2.0.9/maven-core-2.0.9.jar");
    assertTrue( af.exists() );
    assertEquals( 159630, af.length() );
    
    File ap = new File( repo.getDirectory(), "/org/apache/maven/maven-core/2.0.9/maven-core-2.0.9.pom");
    assertTrue( ap.exists() );
    assertEquals( 7785, ap.length() );
    
    
  }

  
}
