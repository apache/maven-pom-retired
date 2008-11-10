package org.apache.maven.mercury.repository.remote.m2;

import java.io.File;

import junit.framework.TestCase;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class RemoteRepositoryUtilM2Test
    extends TestCase
{
  public void testFlipFolder()
  {
    File repo = new File("./target/test-classes/localTestRepo");
    
    RepositoryUtilM2.flipLocalFolderToRemoteRepository( repo );
    
    File ga = new File( repo, "org/apache/maven/mercury/mercury-pom/maven-metadata.xml");
    
    assertTrue( ga.exists() );
    
    File gav = new File( repo, "org/apache/maven/mercury/mercury-pom/1.0.0-alpha-2-SNAPSHOT/maven-metadata.xml");
    
  }
}
