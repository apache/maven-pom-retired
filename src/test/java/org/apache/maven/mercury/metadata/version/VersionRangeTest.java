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

  private static final String rangeSE = "[ 1.2.3 , )"; 
  VersionRange rangeE;

  protected void setUp() throws Exception
  {
    range = new VersionRange(rangeS);
    rangeE = new VersionRange(rangeSE);
  }

  protected void tearDown() throws Exception
  {
  }
  
  public void testSimple()
  {
    assert range.includes( "1.2.4" ) : "1.2.4 did not match the range "+rangeS; 
    assert range.includes( "1.3.1" ) : "1.3.1 did not match the range "+rangeS; 
    assert range.includes( "1.2.3" ) : "1.2.3 did not match the range "+rangeS; 
    assert ! range.includes( "1.2.2" ) : "1.2.2 did matches the range "+rangeS; 
    assert ! range.includes( "2.0.0" ) : "2.0.0 did matches the range "+rangeS; 
    assert ! range.includes( "3.1.0" ) : "3.1.0 did matches the range "+rangeS; 
  }
  
  public void testEternity()
  {
    assert rangeE.includes( "1.2.4" ) : "1.2.4 did not match the range "+rangeS; 
    assert rangeE.includes( "1.3.1" ) : "1.3.1 did not match the range "+rangeS; 
    assert rangeE.includes( "1.2.3" ) : "1.2.3 did not match the range "+rangeS; 
    assert ! rangeE.includes( "1.2.2" ) : "1.2.2 does matches the range "+rangeS; 
    assert rangeE.includes( "2.0.0" ) : "2.0.0 does matches the range "+rangeS; 
    assert rangeE.includes( "3.1.0" ) : "3.1.0 does matches the range "+rangeS; 
  }

}
