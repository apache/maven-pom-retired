package org.apache.maven.mercury.logging;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public abstract class AbstractMercuryLogger
{
  protected String className;
  
  @SuppressWarnings("unchecked")
  public AbstractMercuryLogger( Class clazz )
  {
    className = clazz.getName();
  }
  
  public boolean isDebugEnabled()
  {
    return MercuryLoggerManager._threshold.getId() <= MercuryLoggingLevelEnum.debug.getId();
  }

  public boolean isErrorEnabled()
  {
    return MercuryLoggerManager._threshold.getId() <= MercuryLoggingLevelEnum.error.getId();
  }

  public boolean isFatalEnabled()
  {
    return MercuryLoggerManager._threshold.getId() <= MercuryLoggingLevelEnum.error.getId();
  }

  public boolean isInfoEnabled()
  {
    return MercuryLoggerManager._threshold.getId() <= MercuryLoggingLevelEnum.info.getId();
  }

  public boolean isWarnEnabled()
  {
    return MercuryLoggerManager._threshold.getId() <= MercuryLoggingLevelEnum.warn.getId();
  }
}
