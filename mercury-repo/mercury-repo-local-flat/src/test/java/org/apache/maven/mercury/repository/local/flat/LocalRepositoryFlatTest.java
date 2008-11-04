package org.apache.maven.mercury.repository.local.flat;

import java.io.File;

import org.apache.maven.mercury.util.FileUtil;

import junit.framework.TestCase;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class LocalRepositoryFlatTest
    extends TestCase
{
  File _dir;
  LocalRepositoryFlat _repo;
  
  String repoUrl = "http://repository.sonatype.org/content/groups/public";

  @Override
  protected void setUp()
      throws Exception
  {
    _dir = File.createTempFile( "test-repo-", "-flat" );
    _dir.delete();
    _dir.mkdirs();
    
    _repo = new LocalRepositoryFlat("testFlatRepo", _dir );
  }

}
