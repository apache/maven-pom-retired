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
    in = new RepositoryUpdateIntervalPolicy( RepositoryUpdateIntervalPolicy.UPDATE_POLICY_INTERVAL+"1" );
    now = System.currentTimeMillis();
  }

  public void testInterval()
  {
    assertFalse( in.timeToUpdate( now ) );
    assertTrue( in.timeToUpdate( now-80000L ) );
  }

  public void testDayly()
  {
    in = new RepositoryUpdateIntervalPolicy( RepositoryUpdateIntervalPolicy.UPDATE_POLICY_DAILY );

    assertFalse( in.timeToUpdate( now ) );
    assertFalse( in.timeToUpdate( now-80000L ) );
    assertTrue( in.timeToUpdate( now - 24L*3600L*1000L - 80000L ) );
  }
}
