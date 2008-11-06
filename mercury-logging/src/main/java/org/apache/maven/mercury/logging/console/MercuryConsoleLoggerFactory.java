package org.apache.maven.mercury.logging.console;

import org.apache.maven.mercury.logging.IMercuryLogger;
import org.apache.maven.mercury.logging.IMercuryLoggerFactory;
import org.apache.maven.mercury.logging.MercuryLoggerManager;
import org.apache.maven.mercury.logging.MercuryLoggingLevelEnum;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class MercuryConsoleLoggerFactory
implements IMercuryLoggerFactory
{
  MercuryLoggingLevelEnum _threshold = MercuryLoggerManager.getThreshold();
  
  @SuppressWarnings("unchecked")
  public IMercuryLogger getLogger( Class clazz )
  {
    return new MercuryConsoleLogger(clazz);
  }

  public void setThreshold( MercuryLoggingLevelEnum threshold )
  {
    _threshold = threshold;
  }

}
