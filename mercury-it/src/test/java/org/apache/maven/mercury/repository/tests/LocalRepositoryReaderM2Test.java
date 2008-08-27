package org.apache.maven.mercury.repository.tests;

import java.io.File;
import java.util.ArrayList;

import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;
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
public class LocalRepositoryReaderM2Test
extends AbstractRepositoryReaderM2Test
{

  @Override
  protected void setUp()
  throws Exception
  {
    mdProcessor = new MetadataProcessorMock();

    query = new ArrayList<ArtifactBasicMetadata>();

    server = new Server( "test", new File("./target/test-classes/repo").toURL() );
      
    repo = new LocalRepositoryM2( server );
    reader = repo.getReader( mdProcessor );

    super.setUp();
  }

}
