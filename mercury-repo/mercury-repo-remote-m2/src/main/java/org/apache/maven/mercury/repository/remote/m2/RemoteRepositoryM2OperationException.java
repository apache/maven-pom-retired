package org.apache.maven.mercury.repository.remote.m2;

import org.apache.maven.mercury.repository.api.RepositoryException;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class RemoteRepositoryM2OperationException
extends RepositoryException
{

  /**
   * 
   */
  public RemoteRepositoryM2OperationException()
  {
    // TODO Auto-generated constructor stub
  }

  /**
   * @param message
   */
  public RemoteRepositoryM2OperationException(
      String message )
  {
    super( message );
    // TODO Auto-generated constructor stub
  }

  /**
   * @param cause
   */
  public RemoteRepositoryM2OperationException(
      Throwable cause )
  {
    super( cause );
    // TODO Auto-generated constructor stub
  }

  /**
   * @param message
   * @param cause
   */
  public RemoteRepositoryM2OperationException(
      String message,
      Throwable cause )
  {
    super( message, cause );
    // TODO Auto-generated constructor stub
  }

}
