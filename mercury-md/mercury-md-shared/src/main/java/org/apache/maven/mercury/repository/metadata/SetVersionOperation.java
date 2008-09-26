package org.apache.maven.mercury.repository.metadata;

import org.codehaus.plexus.lang.DefaultLanguage;
import org.codehaus.plexus.lang.Language;

/**
 * adds new snapshot to metadata
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class SetVersionOperation
    implements MetadataOperation
{
  private static final Language lang = new DefaultLanguage( SetVersionOperation.class );
  
  private String version;
  
  /**
   * @throws MetadataException 
   * 
   */
  public SetVersionOperation(  StringOperand data  )
  throws MetadataException
  {
    setOperand( data );
  }
  
  public void setOperand( Object data )
  throws MetadataException
  {
    if( data == null || !(data instanceof StringOperand) )
      throw new MetadataException( lang.getMessage( "bad.operand", "SnapshotOperand", data == null ? "null" : data.getClass().getName() ) );
    
    version = ((StringOperand)data).getOperand();
  }

  /**
   * add/replace snapshot to the in-memory metadata instance
   * 
   * @param metadata
   * @return
   * @throws MetadataException 
   */
  public boolean perform( Metadata metadata )
  throws MetadataException
  {
    if( metadata == null )
      return false;
   
    String vs = metadata.getVersion(); 
    
    if( vs == null )
    {
      if( version == null )
        return false;
    }
    else 
      if( vs.equals( version ) )
        return false;
    
    metadata.setVersion( version );
    
    return true;
  }

}
