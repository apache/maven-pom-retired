package org.apache.maven.mercury.util;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class MercuryMonitor
{
  private final org.slf4j.Logger _log;

  public MercuryMonitor( Class clazz )
  {
    _log = org.slf4j.LoggerFactory.getLogger( clazz ); 
  }
  
  public void info( String msg )
  {
    _log.info( msg );
  }
  
  public boolean isInfoEnabled()
  {
    return _log.isInfoEnabled();
  }
  
  public void warn( String msg )
  {
    _log.warn( msg );
  }
  
  public boolean isWarnEnabled()
  {
    return _log.isWarnEnabled();
  }
  
  public void error( String msg )
  {
    _log.error( msg );
  }
  
  public boolean isErrorEnabled()
  {
    return _log.isErrorEnabled();
  }
  
  public void debug( String msg )
  {
    _log.debug( msg );
  }
  
  public boolean isDebugEnabled()
  {
    return _log.isInfoEnabled();
  }
  
  public void trace( String msg )
  {
    _log.trace( msg );
  }
  
  public boolean isTraceEnabled()
  {
    return _log.isTraceEnabled();
  }

}
