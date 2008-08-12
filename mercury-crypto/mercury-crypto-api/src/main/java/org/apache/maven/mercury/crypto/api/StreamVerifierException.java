package org.apache.maven.mercury.crypto.api;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class StreamVerifierException
extends StreamObserverException
{

  /**
   * 
   */
  public StreamVerifierException()
  {
    // TODO Auto-generated constructor stub
  }

  /**
   * @param message
   */
  public StreamVerifierException( String message )
  {
    super( message );
  }

  /**
   * @param cause
   */
  public StreamVerifierException(
      Throwable cause )
  {
    super( cause );
    // TODO Auto-generated constructor stub
  }

  /**
   * @param message
   * @param cause
   */
  public StreamVerifierException(
      String message,
      Throwable cause )
  {
    super( message, cause );
    // TODO Auto-generated constructor stub
  }

}
