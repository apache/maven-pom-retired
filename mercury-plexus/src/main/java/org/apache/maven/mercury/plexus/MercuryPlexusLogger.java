package org.apache.maven.mercury.plexus;

import org.apache.maven.mercury.logging.IMercuryLogger;
import org.codehaus.plexus.logging.Logger;

/**
 * Mercury adaptor for plexus logger
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class MercuryPlexusLogger
implements IMercuryLogger
{
  Logger _logger;
  
  public MercuryPlexusLogger( Logger logger )
  {
    this._logger = logger;
  }

  public void debug( String message )
  {
    _logger.debug( message );
  }

  public void debug( String message, Throwable throwable )
  {
    _logger.debug( message, throwable );
  }

  public void error( String message )
  {
    _logger.error( message );
  }

  public void error( String message, Throwable throwable )
  {
    _logger.error( message, throwable );
  }

  public void fatal( String message )
  {
    _logger.fatalError( message );
  }

  public void fatal( String message, Throwable throwable )
  {
    _logger.fatalError( message, throwable );
  }

  public void info( String message )
  {
    _logger.info( message );
  }

  public void info( String message, Throwable throwable )
  {
    _logger.info( message, throwable );
  }

  public void warn( String message )
  {
    _logger.warn( message );
  }

  public void warn( String message, Throwable throwable )
  {
    _logger.warn( message, throwable );
  }

  public boolean isDebugEnabled()
  {
    return _logger.isDebugEnabled();
  }

  public boolean isErrorEnabled()
  {
    return _logger.isErrorEnabled();
  }

  public boolean isFatalEnabled()
  {
    return _logger.isFatalErrorEnabled();
  }

  public boolean isInfoEnabled()
  {
    return _logger.isInfoEnabled();
  }

  public boolean isWarnEnabled()
  {
    return _logger.isWarnEnabled();
  }
}
