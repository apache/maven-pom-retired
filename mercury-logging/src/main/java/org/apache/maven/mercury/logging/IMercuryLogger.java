package org.apache.maven.mercury.logging;

/**
 * A copy of plexus default container logger interface. Need it to externalize the logging system
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public interface IMercuryLogger
{
  void debug( String message );

  void debug( String message, Throwable throwable );

  boolean isDebugEnabled();

  void info( String message );

  void info( String message, Throwable throwable );

  boolean isInfoEnabled();

  void warn( String message );

  void warn( String message, Throwable throwable );

  boolean isWarnEnabled();

  void error( String message );

  void error( String message, Throwable throwable );

  boolean isErrorEnabled();

  void fatal( String message );

  void fatal( String message, Throwable throwable );

  boolean isFatalEnabled();
}
