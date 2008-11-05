package org.apache.maven.mercury.logging.console;

import org.apache.maven.mercury.logging.AbstractMercuryLogger;
import org.apache.maven.mercury.logging.MercuryLoggingLevelEnum;
import org.apache.maven.mercury.logging.IMercuryLogger;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class MercuryConsoleLogger
extends AbstractMercuryLogger
implements IMercuryLogger
{
  
  @SuppressWarnings("unchecked")
  public MercuryConsoleLogger( Class clazz )
  {
    super( clazz );
  }
  
  private static final void say( MercuryLoggingLevelEnum level, String message, Throwable throwable )
  {
    System.out.print( "["+level.name()+"] " );
    System.out.println( message );
    if( throwable != null )
    {
      throwable.printStackTrace( System.out );
    }
  }

  public void debug( String message )
  {
    if( isDebugEnabled() )
      say( MercuryLoggingLevelEnum.debug, message, null );
  }

  public void debug( String message, Throwable throwable )
  {
    if( isDebugEnabled() )
      say( MercuryLoggingLevelEnum.debug, message, throwable );
  }

  /* (non-Javadoc)
   * @see org.apache.maven.mercury.logging.MercuryLogger#error(java.lang.String)
   */
  public void error(
      String message )
  {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see org.apache.maven.mercury.logging.MercuryLogger#error(java.lang.String, java.lang.Throwable)
   */
  public void error(
      String message,
      Throwable throwable )
  {
    // TODO Auto-generated method stub

  }

  public void fatal( String message )
  {
  }

  public void fatal( String message, Throwable throwable )
  {
  }

  public int getThreshold()
  {
    return 0;
  }

  public void info( String message )
  {
  }

  public void info(
      String message,
      Throwable throwable )
  {
    // TODO Auto-generated method stub

  }

  public void warn( String message )
  {
    // TODO Auto-generated method stub

  }

  public void warn( String message, Throwable throwable )
  {
    // TODO Auto-generated method stub

  }

}
