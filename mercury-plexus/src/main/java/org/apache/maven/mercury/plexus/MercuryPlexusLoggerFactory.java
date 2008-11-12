package org.apache.maven.mercury.plexus;

import org.apache.maven.mercury.logging.IMercuryLogger;
import org.apache.maven.mercury.logging.IMercuryLoggerFactory;
import org.apache.maven.mercury.logging.MercuryLoggingLevelEnum;
import org.codehaus.plexus.logging.LoggerManager;

/**
 * mercury adaptor for plesux logger factory (manager)
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class MercuryPlexusLoggerFactory
implements IMercuryLoggerFactory
{
  LoggerManager _loggerManager;
  
  public MercuryPlexusLoggerFactory( LoggerManager loggerManager )
  {
    setLoggerFactory( loggerManager );
  }

  public IMercuryLogger getLogger( Class clazz )
  {
    return new MercuryPlexusLogger( _loggerManager.getLoggerForComponent( clazz.getName() ) );
  }

  public void setLoggerFactory( LoggerManager loggerManager )
  {
    this._loggerManager = loggerManager;
  }

  public void setThreshold( MercuryLoggingLevelEnum threshold )
  {
    // TODO Auto-generated method stub
    
  }

}
