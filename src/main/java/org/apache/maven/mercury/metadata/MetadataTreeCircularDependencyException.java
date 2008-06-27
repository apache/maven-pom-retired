package org.apache.maven.mercury.metadata;

/**
 * 
 * @author Oleg Gusakov
 * @version $Id$
 */
public class MetadataTreeCircularDependencyException
extends MetadataTreeException
{

  public MetadataTreeCircularDependencyException()
  {
  }

  public MetadataTreeCircularDependencyException(String message)
  {
    super(message);
  }

  public MetadataTreeCircularDependencyException(Throwable cause)
  {
    super(cause);
  }

  public MetadataTreeCircularDependencyException(String message, Throwable cause)
  {
    super(message, cause);
  }

}
