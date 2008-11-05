package org.apache.maven.mercury.logging;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public enum MercuryLoggingLevelEnum
{
    debug(0)
  , info(1)
  , warn(2)
  , error(3)
  , fatal(4)
  , disabled(5)
  ;

  public static final MercuryLoggingLevelEnum DEFAULT_LEVEL = error;

  private int id;

  // Constructor
  MercuryLoggingLevelEnum( int id )
  {
      this.id = id;
  }

  int getId()
  {
      return id;
  }

}
