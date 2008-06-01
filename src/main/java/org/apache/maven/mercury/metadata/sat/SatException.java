package org.apache.maven.mercury.metadata.sat;

/**
 * @author <a href="oleg@codehaus.org">Oleg Gusakov</a>
 */
public class SatException
    extends Exception
{
  private static final long serialVersionUID = -2461839690564604496L;

  public SatException()
  {
  }

  public SatException(String message)
  {
    super(message);
  }

  public SatException(Throwable cause)
  {
    super(cause);
  }

  public SatException(String message, Throwable cause)
  {
    super(message, cause);
  }

}
