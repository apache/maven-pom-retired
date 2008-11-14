package org.apache.maven.mercury.builder.api;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class DependencyProcessorException
extends Exception
{

  /**
   * 
   */
  public DependencyProcessorException()
  {
  }

  /**
   * @param message
   */
  public DependencyProcessorException( String message )
  {
    super( message );
  }

  /**
   * @param cause
   */
  public DependencyProcessorException( Throwable cause )
  {
    super( cause );
  }

  /**
   * @param message
   * @param cause
   */
  public DependencyProcessorException( String message, Throwable cause )
  {
    super( message, cause );
  }

}
