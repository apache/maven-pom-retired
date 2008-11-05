package org.apache.maven.mercury.logging.console;

import org.apache.maven.mercury.logging.IMercuryLogger;
import org.apache.maven.mercury.logging.IMercuryLoggerFactory;

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
  @SuppressWarnings("unchecked")
  public IMercuryLogger getLogger( Class clazz )
  {
    return new MercuryConsoleLogger(clazz);
  }

}
