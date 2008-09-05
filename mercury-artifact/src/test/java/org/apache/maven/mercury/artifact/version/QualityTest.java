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
    
  }
}
