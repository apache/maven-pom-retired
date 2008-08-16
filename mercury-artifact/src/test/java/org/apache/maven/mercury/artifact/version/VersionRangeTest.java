package org.apache.maven.mercury.artifact.version;

import org.apache.maven.mercury.artifact.Quality;
import org.apache.maven.mercury.artifact.QualityRange;

import junit.framework.TestCase;

/**
 * 
 * @author Oleg Gusakov
 * @version $Id$
 */
public class VersionRangeTest
    extends TestCase
{
  VersionRange range;

  public void testSimple()
  throws VersionException
  {
    String rangeS = "[ 1.2.3 , 2.0.0 )";
    range = new VersionRange( rangeS );

    assertTrue(  "1.2.4 did not match the range "+rangeS, range.includes( "1.2.4" ) ); 
    assertTrue(  "1.3.1 did not match the range "+rangeS, range.includes( "1.3.1" ) ); 
    assertTrue(  "1.2.3 did not match the range "+rangeS, range.includes( "1.2.3" ) ); 
    assertFalse(  "1.2.2 did matches the range "+rangeS, range.includes( "1.2.2" ) ); 
    assertFalse(  "2.0.0 did matches the range "+rangeS, range.includes( "2.0.0" ) ); 
    assertFalse(  "3.1.0 did matches the range "+rangeS, range.includes( "3.1.0" ) ); 
  }
  
  public void testEternity()
  throws VersionException
  {
    String rangeS = "[ 1.2.3 , )";
    range = new VersionRange( rangeS );

    assertTrue(  "1.2.4 did not match the range "+rangeS, range.includes( "1.2.4" ) ); 
    assertTrue(  "1.3.1 did not match the range "+rangeS, range.includes( "1.3.1" ) ); 
    assertTrue(  "1.2.3 did not match the range "+rangeS, range.includes( "1.2.3" ) ); 
    assertFalse(  "1.2.2 does matches the range "+rangeS, range.includes( "1.2.2" ) ); 
    assertTrue(  "2.0.0 does matches the range "+rangeS, range.includes( "2.0.0" ) ); 
    assertTrue(  "3.1.0 does matches the range "+rangeS, range.includes( "3.1.0" ) ); 
  }
  
  public void test6Digits()
  throws VersionException
  {
    String rangeS = "[ 1.0.0.1.2.1 , )";
    range = new VersionRange( rangeS );

    assertTrue(  "1.0.0.1.2.1 did not match the range "+rangeS, range.includes( "1.0.0.1.2.1" ) );
    assertTrue(  "1.0.0.1.2.2 did not match the range "+rangeS, range.includes( "1.0.0.1.2.2" ) );
    assertTrue(  "1.0.0.1.3.0 did not match the range "+rangeS, range.includes( "1.0.0.1.3.0" ) );
    assertFalse(  "1.0.0.1.2.0 does matches the range "+rangeS, range.includes( "1.0.0.1.2.0" ) );
    assertFalse( "1.0.0.1.2.1-alpha-1 does match the range "+rangeS, range.includes( "1.0.0.1.2.1-alpha-1" ) );
    assertTrue(  "1.0.0.1.2.2-alpha-1 does not match the range "+rangeS, range.includes( "1.0.0.1.2.2-alpha-1" ) );
  }
  
  public void testAlphaNumeric()
  throws VersionException
  {
    String rangeS = "[1.0.0.0.22,)";
    range = new VersionRange( rangeS );

    assertFalse( "1.0.0.0.9 does match the range "+rangeS, range.includes( "1.0.0.0.9" ) );
  }
  
  public void testEdge()
  throws VersionException
  {
    String rangeS = "[1.0,2.0)";
    range = new VersionRange( rangeS );

    assertFalse( range.includes( "1.0-SNAPSHOT" ) );
    assertTrue( range.includes( "1.1-SNAPSHOT" ) );
    assertTrue( range.includes( "2.0-SNAPSHOT" ) );
    assertTrue( range.includes( "2.0-alpha-1" ) );
    assertFalse( range.includes( "2.0" ) );
  }
  
  public void testBetaEdge()
  throws VersionException
  {
    String rangeS = "[1.0,2.0)";
    range = new VersionRange( rangeS );
    range.setToQualityRange( new QualityRange( Quality.BETA_QUALITY, true, Quality.RELEASE_QUALITY, true  ) );

    assertFalse( range.includes( "1.0-SNAPSHOT" ) );
    assertTrue( range.includes( "1.1-SNAPSHOT" ) );
    assertFalse( range.includes( "2.0-SNAPSHOT" ) );
    assertTrue( range.includes( "2.0-beta-1" ) );
    assertFalse( range.includes( "2.0" ) );
  }
  
  public void testAlphaEdge()
  throws VersionException
  {
    String rangeS = "[1.0,2.0)";
    range = new VersionRange( rangeS );
    range.setToQualityRange( new QualityRange( Quality.ALPHA_QUALITY, true, Quality.RELEASE_QUALITY, true  ) );

    assertFalse( range.includes( "1.0-SNAPSHOT" ) );
    assertTrue( range.includes( "1.1-SNAPSHOT" ) );
    assertFalse( range.includes( "2.0-SNAPSHOT" ) );
    assertTrue( range.includes( "2.0-alpha-35" ) );
    assertTrue( range.includes( "2.0-beta-1" ) );
    assertFalse( range.includes( "2.0" ) );
  }

}
