package org.apache.maven.wagon.mercury;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;

import org.apache.maven.mercury.crypto.api.StreamVerifierAttributes;
import org.apache.maven.mercury.crypto.api.StreamVerifierFactory;
import org.apache.maven.mercury.crypto.pgp.PgpStreamVerifierFactory;
import org.apache.maven.mercury.crypto.sha.SHA1VerifierFactory;
import org.apache.maven.mercury.transport.api.Credentials;
import org.apache.maven.mercury.transport.api.Server;
import org.apache.maven.mercury.util.FileUtil;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonTestCase;
import org.codehaus.plexus.PlexusContainer;
import org.sonatype.appbooter.ForkedAppBooter;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class MercuryWagonTest
extends WagonTestCase
{

  public static final String TEST_NEXUS_ROLE = ForkedAppBooter.ROLE;
  public static final String TEST_NEXUS_HINT = "NexusForkedAppBooter";

  PlexusContainer plexus;
  ForkedAppBooter nexusForkedAppBooter;
  
  String nexusReleasesTestDir = "./target/nexus-webapp-1.0.0/runtime/work/storage/releases";
  String nexusReleasesTestUrl = "http://127.0.0.1:8091/nexus/content/repositories/releases";

  String nexusSnapshotsTestDir = "./target/nexus-webapp-1.0.0/runtime/work/storage/snapshots";
  String nexusSnapshotsTestUrl = "http://127.0.0.1:8091/nexus/content/repositories/snapshots";

  String nexusTestUser = "admin";
  String nexusTestPass = "admin123";
  
  protected static final String keyId   = "0EDB5D91141BC4F2";

  protected static final String secretKeyFile = "/pgp/secring.gpg";
  protected static final String publicKeyFile = "/pgp/pubring.gpg";
  protected static final String secretKeyPass = "testKey82";
  
  PgpStreamVerifierFactory pgpF;
  SHA1VerifierFactory      sha1F;
  HashSet<StreamVerifierFactory> vFacPgp;
  HashSet<StreamVerifierFactory> vFacSha1;
  
  Server server;

  HashSet<StreamVerifierFactory> readFactories;
  HashSet<StreamVerifierFactory> writeFactories;

  private File targetDirectory;
  
  MercuryWagon wagon;
  
  //------------------------------------------------------------------------------
  @Override
  protected void setUp()
  throws Exception
  {
    Credentials user = new Credentials( nexusTestUser, nexusTestPass );

    server = new Server( "mercuryWagonTestRead", new URL(nexusReleasesTestUrl), false, false, user );
    
    // verifiers
    readFactories = new HashSet<StreamVerifierFactory>();       
    readFactories.add( 
        new PgpStreamVerifierFactory(
                new StreamVerifierAttributes( PgpStreamVerifierFactory.DEFAULT_EXTENSION, false, false )
                , getClass().getResourceAsStream( publicKeyFile )
                                    )
                  );
    readFactories.add( new SHA1VerifierFactory(false,false) );
    server.setWriterStreamVerifierFactories(readFactories);

    // verifiers
    writeFactories = new HashSet<StreamVerifierFactory>();       
    writeFactories.add( 
        new PgpStreamVerifierFactory(
                new StreamVerifierAttributes( PgpStreamVerifierFactory.DEFAULT_EXTENSION, false, false )
                , getClass().getResourceAsStream( secretKeyFile )
                , keyId
                , secretKeyPass
                                    )
                  );
    writeFactories.add( new SHA1VerifierFactory(false,false) );
    server.setWriterStreamVerifierFactories(writeFactories);
      
    targetDirectory = new File(nexusReleasesTestDir);
    FileUtil.delete( new File( targetDirectory, "org" ) );
    
    wagon = new MercuryWagon( server );

    super.setUp();

    plexus = getContainer();

    assertNotNull( plexus );
    
    nexusForkedAppBooter = (ForkedAppBooter)plexus.lookup( TEST_NEXUS_ROLE, TEST_NEXUS_HINT  );
    
    nexusForkedAppBooter.start();

  }
  
  @Override
  protected void tearDown()
  throws Exception
  {
    super.tearDown();
    nexusForkedAppBooter.stop();
  }
  
  @Override
  protected Wagon getWagon()
      throws Exception
  {
    return wagon;
  }



  @Override
  protected String getProtocol()
  {
    return "http";
  }

  @Override
  protected String getTestRepositoryUrl()
      throws IOException
  {
    return nexusReleasesTestUrl;
  }

}
