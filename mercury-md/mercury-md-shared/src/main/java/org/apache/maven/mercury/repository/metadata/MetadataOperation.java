package org.apache.maven.mercury.repository.metadata;

/**
 * change of a Metadata object
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public interface MetadataOperation
{
  /** sets the operation's data */
  public void setOperand( Object data )
  throws MetadataException;
  
  /**
   * performs the operation
   *
   * @param metadata to perform on
   * @return true if operation changed the data
   */
  public boolean perform( Metadata metadata )
  throws MetadataException;
}
