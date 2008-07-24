package org.apache.maven.mercury.repository.api;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class RepositoryException
    extends Exception
{

  /**
   * 
   */
  private static final long serialVersionUID = -1193169088723411771L;

  /**
   * 
   */
  public RepositoryException()
  {
    // TODO Auto-generated constructor stub
  }

  /**
   * @param message
   */
  public RepositoryException(
      String message )
  {
    super( message );
    // TODO Auto-generated constructor stub
  }

  /**
   * @param cause
   */
  public RepositoryException(
      Throwable cause )
  {
    super( cause );
    // TODO Auto-generated constructor stub
  }

  /**
   * @param message
   * @param cause
   */
  public RepositoryException(
      String message,
      Throwable cause )
  {
    super( message, cause );
    // TODO Auto-generated constructor stub
  }

}
