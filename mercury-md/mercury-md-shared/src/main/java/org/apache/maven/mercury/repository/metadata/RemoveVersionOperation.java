package org.apache.maven.mercury.repository.metadata;

import java.util.List;

import org.codehaus.plexus.i18n.DefaultLanguage;
import org.codehaus.plexus.i18n.Language;

/**
 * removes a version from Metadata
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class RemoveVersionOperation
implements MetadataOperation
{
  private static final Language lang = new DefaultLanguage( RemoveVersionOperation.class );
  
  private String version;
  
  /**
   * @throws MetadataException 
   * 
   */
  public RemoveVersionOperation(  Object data  )
  throws MetadataException
  {
    setOperand( data );
  }
  
  public void setOperand( Object data )
  throws MetadataException
  {
    if( data == null || !(data instanceof StringOperand) )
      throw new MetadataException( lang.getMessage( "bad.operand", "StringOperand", data == null ? "null" : data.getClass().getName() ) );
    
    version = ((StringOperand)data).getOperand();
  }

  /**
   * remove version to the in-memory metadata instance
   * 
   * @param metadata
   * @param version
   * @return
   */
  public boolean perform( Metadata metadata )
  throws MetadataException
  {
    if( metadata == null )
      return false;
    
    Versioning vs = metadata.getVersioning(); 
    
    if( vs == null )
    {
      return false;
    }
    
    if( vs.getVersions() != null && vs.getVersions().size() > 0 )
    {
      List<String> vl = vs.getVersions();
      if( ! vl.contains( version ) )
        return false;
    }
    
    vs.removeVersion( version );
    vs.setLastUpdated( MetadataBuilder.getUTCTimestamp() );
    
    return true;
  }

}
