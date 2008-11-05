package org.apache.maven.mercury.logging;

import org.apache.maven.mercury.logging.console.MercuryConsoleLoggerFactory;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class MercuryLoggerManager
{
  public static final String SYSTEM_PROPERTY_MERCURY_LOG_FACTORY = "maven.mercury.log.factory";
  
  public static final String _loggerFactoryClassName = System.getProperty( SYSTEM_PROPERTY_MERCURY_LOG_FACTORY, MercuryConsoleLoggerFactory.class.getName() );
  
  static MercuryLoggingLevelEnum _threshold = MercuryLoggingLevelEnum.error;

  static IMercuryLoggerFactory _loggerFactory;
  
  @SuppressWarnings("unchecked")
  public static final IMercuryLogger getLogger( Class clazz )
  {
    if( _loggerFactory == null )
    {
      try
      {
        _loggerFactory = (IMercuryLoggerFactory)Class.forName( _loggerFactoryClassName ).newInstance();
      }
      catch( Exception e )
      {
        _loggerFactory = new MercuryConsoleLoggerFactory();
        _loggerFactory.getLogger( MercuryLoggerManager.class ).error( "cannot load logger for "+_loggerFactoryClassName, e );
      }
    }

    return _loggerFactory.getLogger( clazz );
  }

  public static void setThreshold( MercuryLoggingLevelEnum threshold )
  {
    _threshold = threshold;
  }
  
  public static MercuryLoggingLevelEnum getThreshold()
  {
    return _threshold;
  }
}
