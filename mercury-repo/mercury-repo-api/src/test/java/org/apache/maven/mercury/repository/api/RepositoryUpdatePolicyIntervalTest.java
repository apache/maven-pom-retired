package org.apache.maven.mercury.repository.api;

import junit.framework.TestCase;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class RepositoryUpdatePolicyIntervalTest
    extends TestCase
{
  RepositoryUpdateIntervalPolicy in;
  long now;
  
  @Override
  protected void setUp()
      throws Exception
  {
    in = new RepositoryUpdateIntervalPolicy( RepositoryUpdateIntervalPolicy.UPDATE_POLICY_NAME_INTERVAL+"1" );
    now = System.currentTimeMillis();
  }

  public void testInterval()
  {
    assertFalse( in.timestampExpired( now ) );
    assertTrue( in.timestampExpired( now-80000L ) );
  }

  public void testDayly()
  {
    in = new RepositoryUpdateIntervalPolicy( RepositoryUpdateIntervalPolicy.UPDATE_POLICY_NAME_DAILY );

    assertFalse( in.timestampExpired( now ) );
    assertFalse( in.timestampExpired( now-80000L ) );
    assertTrue( in.timestampExpired( now - 24L*3600L*1000L - 80000L ) );
  }
}
