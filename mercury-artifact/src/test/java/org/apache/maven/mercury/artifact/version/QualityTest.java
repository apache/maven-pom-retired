package org.apache.maven.mercury.artifact.version;

import junit.framework.TestCase;

import org.apache.maven.mercury.artifact.Quality;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class QualityTest
    extends TestCase
{
  Quality q;
  
  public void testEquality()
  {
    q = new Quality("LATEST");
    assertTrue( q.equals( Quality.FIXED_LATEST_QUALITY ) );
    
    q = new Quality("RELEASE");
    assertTrue( q.equals( Quality.FIXED_RELEASE_QUALITY ) );
    
    q = new Quality("1.1-SNAPSHOT");
    assertTrue( q.equals( Quality.SNAPSHOT_QUALITY ) );
  }
}
