package org.apache.maven.mercury.artifact;

import junit.framework.TestCase;

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
  
  public void testParser()
  {
    Quality q = new Quality("a-b_a-1");
    
    assertNotNull( q.quality );
    assertEquals( QualityEnum.release, q.getQuality() );
    assertEquals( Quality.DEFAULT_QUANTITY, q.getQuantity() );
  }
  
  public void testSn1()
  {
    Quality q = new Quality("5.5-SNAPSHOT");
    
    assertNotNull( q.quality );
    assertEquals( QualityEnum.snapshot, q.getQuality() );
    assertEquals( Quality.DEFAULT_QUANTITY, q.getQuantity() );
  }
  
  public void testSn2()
  {
    Quality q = new Quality("5.5-20080815.231708-12");
    
    assertNotNull( q.quality );
    assertEquals( QualityEnum.snapshot, q.getQuality() );
    assertEquals( Quality.SNAPSHOT_TS_QUANTITY, q.getQuantity() );
  }
  
  public void testSn3()
  {
    Quality q = new Quality("5.5-20080815.231708-");
    
    assertNotNull( q.quality );
    assertEquals( QualityEnum.release, q.getQuality() );
  }
  
  public void testSn4()
  {
    Quality q = new Quality("5.5-20080815.23-15");
    
    assertNotNull( q.quality );
    assertEquals( QualityEnum.release, q.getQuality() );
  }
  
  public void testAlpha()
  {
    Quality q = new Quality("5.5-alpha-12");
    
    assertNotNull( q.quality );
    assertEquals( QualityEnum.alpha, q.getQuality() );
    assertEquals( 12, q.getQuantity() );
  }
  
  public void testBeta()
  {
    Quality q = new Quality("5.5-beta-1");
    
    assertNotNull( q.quality );
    assertEquals( QualityEnum.beta, q.getQuality() );
    assertEquals( 1, q.getQuantity() );
  }
  
  public void testCompateQ()
  {
    Quality u   = Quality.UNKNOWN_QUALITY;
    Quality sn   = new Quality("5.5-SNAPSHOT");
    Quality snts = new Quality("5.5-5.5-20080815.231708-12");
    Quality a1 = new Quality("5.5-5.5-alpha-1");
    Quality a2 = new Quality("5.5-5.5-alpha-20");
    Quality b1 = new Quality("5.5-5.5-beta-1");
    Quality b2 = new Quality("5.5-5.5-beta-10");
    Quality r  = new Quality("5.5");

    assertTrue( u.compareTo( sn ) < 0 );
    assertTrue( sn.compareTo( snts ) == 0 );
    assertTrue( snts.compareTo( a1 ) < 0 );
    assertEquals( 0, a1.compareTo( a1 ) );
    assertTrue( a1.compareTo( a2 ) < 0 );
    assertTrue( a2.compareTo( b1 ) < 0 );
    assertTrue( b1.compareTo( b2 ) < 0 );
    assertTrue( b2.compareTo( r ) < 0 );
    assertEquals( 0, r.compareTo( r ) );
  }
}
