package org.apache.maven.mercury.metadata.version;

import junit.framework.TestCase;

/**
 * 
 * @author Oleg Gusakov
 * @version $Id$
 */
public class VersionRangeTest
    extends TestCase
{
  private static final String rangeS = "[ 1.2.3 , 2.0.0 )"; 
  VersionRange range;

  protected void setUp() throws Exception
  {
    range = new VersionRange(rangeS);
  }

  protected void tearDown() throws Exception
  {
  }
  
  public void testSimple()
  {
    assert range.isInRange( "1.2.4" ) : "1.2.4 did not match the range "+rangeS; 
    assert range.isInRange( "1.3.1" ) : "1.3.1 did not match the range "+rangeS; 
    assert range.isInRange( "1.2.3" ) : "1.2.3 did not match the range "+rangeS; 
    assert ! range.isInRange( "1.2.2" ) : "1.2.2 did matches the range "+rangeS; 
    assert ! range.isInRange( "2.0.0" ) : "2.0.0 did matches the range "+rangeS; 
    assert ! range.isInRange( "3.1.0" ) : "3.1.0 did matches the range "+rangeS; 
  }

}
