package org.apache.maven.mercury.transport.api;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class TransportException
    extends Exception
{

  /**
   * 
   */
  public TransportException()
  {
    // TODO Auto-generated constructor stub
  }

  /**
   * @param message
   */
  public TransportException(
      String message )
  {
    super( message );
    // TODO Auto-generated constructor stub
  }

  /**
   * @param cause
   */
  public TransportException(
      Throwable cause )
  {
    super( cause );
    // TODO Auto-generated constructor stub
  }

  /**
   * @param message
   * @param cause
   */
  public TransportException(
      String message,
      Throwable cause )
  {
    super( message, cause );
    // TODO Auto-generated constructor stub
  }

}
