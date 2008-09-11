package org.apache.maven.mercury.repository.tests;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashSet;

import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;
import org.apache.maven.mercury.crypto.api.StreamVerifierAttributes;
import org.apache.maven.mercury.crypto.api.StreamVerifierFactory;
import org.apache.maven.mercury.crypto.pgp.PgpStreamVerifierFactory;
import org.apache.maven.mercury.crypto.sha.SHA1VerifierFactory;
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
public class LocalRepositoryWriterM2Test
extends AbstractRepositoryWriterM2Test
{
  public static final String SYSTEM_PARAMETER_SKIP_LOCK_TESTS = "maven.mercury.tests.skip.lock";
  boolean skipLockTests = Boolean.parseBoolean( System.getProperty( SYSTEM_PARAMETER_SKIP_LOCK_TESTS, "true" ) );
  
  //------------------------------------------------------------------------------
  @Override
  protected void setUp()
  throws Exception
  {
    super.setUp();

    targetDirectory = new File("./target/test-classes/tempRepo");
    FileUtil.copy( new File("./target/test-classes/repo"), targetDirectory, true );
    FileUtil.delete( new File(targetDirectory, "org") );
    
    mdProcessor = new MetadataProcessorMock();
    
    query = new ArrayList<ArtifactBasicMetadata>();
    
    server = new Server( "test", targetDirectory.toURL() );
    // verifiers
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
    server.setWriterStreamVerifierFactories(factories);
      
    repo = new LocalRepositoryM2( server );
    reader = repo.getReader( mdProcessor );
    writer = repo.getWriter();
  }

  @Override
  void setReleases()
      throws MalformedURLException
  {
  }

  @Override
  void setSnapshots()
      throws MalformedURLException
  {
  }
  //-------------------------------------------------------------------------
  @Override
  public void testWriteContentionMultipleArtifacts()
      throws Exception
  {
    if( skipLockTests )
      System.out.println("Mutliple Artifacts contention test fails for local repo. Currently there is no way to synchronize those writes");
    else
      super.testWriteContentionMultipleArtifacts();
  }
  
  @Override
  public void testWriteContentionSingleArtifact()
      throws Exception
  {
    if( skipLockTests )
      System.out.println("Single Artifacts contention test fails for remote repo. Currently there is no way to synchronize those writes");
    else
      super.testWriteContentionSingleArtifact();
  }
  
}
