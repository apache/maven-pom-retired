package org.apache.maven.mercury.metadata.version;

/**
 * 
 * @author Oleg Gusakov
 * @version $Id$
 */
public class VersionException
    extends Exception
{

  public VersionException()
  {
  }

  public VersionException(String message)
  {
    super(message);
  }

  public VersionException(Throwable cause)
  {
    super(cause);
  }

  public VersionException(String message, Throwable cause)
  {
    super(message, cause);
  }

}
