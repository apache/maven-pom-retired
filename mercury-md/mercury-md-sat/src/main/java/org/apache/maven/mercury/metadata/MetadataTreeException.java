package org.apache.maven.mercury.metadata;

/**
 * 
 * @author Oleg Gusakov
 * @version $Id$
 */
public class MetadataTreeException
    extends Exception
{

  public MetadataTreeException()
  {
  }

  public MetadataTreeException(String message)
  {
    super(message);
  }

  public MetadataTreeException(Throwable cause)
  {
    super(cause);
  }

  public MetadataTreeException(String message, Throwable cause)
  {
    super(message, cause);
  }

}
