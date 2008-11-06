package org.apache.maven.mercury.logging;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public interface IMercuryLoggerFactory
{
  @SuppressWarnings("unchecked")
  IMercuryLogger getLogger( Class clazz );
  
  void setThreshold( MercuryLoggingLevelEnum threshold );
  
}
