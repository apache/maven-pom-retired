package org.apache.maven.mercury.artifact;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class ConflictException
    extends Exception
{

  /**
   * 
   */
  public ConflictException()
  {
    // TODO Auto-generated constructor stub
  }

  /**
   * @param message
   */
  public ConflictException(
      String message )
  {
    super( message );
    // TODO Auto-generated constructor stub
  }

  /**
   * @param cause
   */
  public ConflictException(
      Throwable cause )
  {
    super( cause );
    // TODO Auto-generated constructor stub
  }

  /**
   * @param message
   * @param cause
   */
  public ConflictException(
      String message,
      Throwable cause )
  {
    super( message, cause );
    // TODO Auto-generated constructor stub
  }

}
