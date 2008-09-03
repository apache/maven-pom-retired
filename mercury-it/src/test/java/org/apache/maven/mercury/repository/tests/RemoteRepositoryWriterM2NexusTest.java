package org.apache.maven.mercury.repository.tests;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;

import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;
import org.apache.maven.mercury.crypto.api.StreamVerifierAttributes;
import org.apache.maven.mercury.crypto.api.StreamVerifierFactory;
import org.apache.maven.mercury.crypto.pgp.PgpStreamVerifierFactory;
import org.apache.maven.mercury.crypto.sha.SHA1VerifierFactory;
import org.apache.maven.mercury.repository.local.m2.MetadataProcessorMock;
import org.apache.maven.mercury.repository.remote.m2.RemoteRepositoryM2;
import org.apache.maven.mercury.transport.api.Credentials;
import org.apache.maven.mercury.transport.api.Server;
import org.apache.maven.mercury.util.FileUtil;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class RemoteRepositoryWriterM2NexusTest
extends AbstractRepositoryWriterM2Test
{
//  String nexusReleasesTestDir = "/app/nexus/storage/test";
//  String nexusReleasesTestUrl = "http://127.0.0.1:8081/nexus/content/repositories/test";

  String nexusReleasesTestDir = "./target/nexus-webapp-1.0.0/runtime/work/storage/releases";
  String nexusReleasesTestUrl = "http://127.0.0.1:8091/nexus/content/repositories/releases";

  String nexusSnapshotsTestDir = "./target/nexus-webapp-1.0.0/runtime/work/storage/snapshots";
  String nexusSnapshotsTestUrl = "http://127.0.0.1:8091/nexus/content/repositories/snapshots";

  String nexusTestUser = "admin";
  String nexusTestPass = "admin123";
  //------------------------------------------------------------------------------
  @Override
  void setReleases()
  throws MalformedURLException
  {
    targetDirectory = new File(nexusReleasesTestDir);
    FileUtil.delete( new File( targetDirectory, "org" ) );
    server.setURL( new URL(nexusReleasesTestUrl) );
  }
  //------------------------------------------------------------------------------
  @Override
  void setSnapshots()
  throws MalformedURLException
  {
    targetDirectory = new File( nexusSnapshotsTestDir );
    FileUtil.delete( new File( targetDirectory, "org" ) );
    server.setURL( new URL( nexusSnapshotsTestUrl ) );
  }
  //------------------------------------------------------------------------------
  @Override
  protected void setUp()
  throws Exception
  {
    super.setUp();

    mdProcessor = new MetadataProcessorMock();

    query = new ArrayList<ArtifactBasicMetadata>();
    
    Credentials user = new Credentials( nexusTestUser, nexusTestPass );

    server = new Server( "nexusTest", new URL(nexusSnapshotsTestUrl), false, false, user );
    
    repo = new RemoteRepositoryM2( "testNexusRepo", server );
    
    mdProcessor = new MetadataProcessorMock();
    
    query = new ArrayList<ArtifactBasicMetadata>();
    
    // verifiers
    factories = new HashSet<StreamVerifierFactory>();       
    factories.add( 
        new PgpStreamVerifierFactory(
                new StreamVerifierAttributes( PgpStreamVerifierFactory.DEFAULT_EXTENSION, false, false )
                , getClass().getResourceAsStream( secretKeyFile )
                , keyId
                , secretKeyPass
                                    )
                  );
    factories.add( new SHA1VerifierFactory(false,false) );
    server.setWriterStreamVerifierFactories(factories);
      
    reader = repo.getReader( mdProcessor );
    writer = repo.getWriter();
    
    setSnapshots();
  }
  //-------------------------------------------------------------------------
  @Override
  protected void tearDown()
  throws Exception
  {
    super.tearDown();
  }
  //-------------------------------------------------------------------------
  //-------------------------------------------------------------------------
}
