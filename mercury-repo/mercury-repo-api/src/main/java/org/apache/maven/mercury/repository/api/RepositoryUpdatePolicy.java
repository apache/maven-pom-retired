package org.apache.maven.mercury.repository.api;

/**
 * abstraction of a repository update policy calculator
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public interface RepositoryUpdatePolicy
{
  /**
   * initialize this calculator
   * 
   * @param policy as a string somewhere in configuration
   */
  void init( String policy );

  /**
   * perform the calculation and decide if it's time to update
   * 
   * @param timestamp - UTC-based timestamp
   * @return
   */
  boolean timestampExpired( long timestamp );
}
