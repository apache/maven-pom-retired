package org.apache.maven.mercury.repository.metadata;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class MetadataException
    extends Exception
{

  /**
   * 
   */
  public MetadataException()
  {
  }

  /**
   * @param message
   */
  public MetadataException(
      String message )
  {
    super( message );
  }

  /**
   * @param cause
   */
  public MetadataException(
      Throwable cause )
  {
    super( cause );
  }

  /**
   * @param message
   * @param cause
   */
  public MetadataException(
      String message,
      Throwable cause )
  {
    super( message, cause );
  }

}
