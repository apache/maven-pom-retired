package org.apache.maven.mercury.repository.metadata;

import java.util.List;

import org.codehaus.plexus.i18n.DefaultLanguage;
import org.codehaus.plexus.i18n.Language;

/**
 * adds new version to metadata
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class AddVersionOperation
    implements MetadataOperation
{
  private static final Language lang = new DefaultLanguage( AddVersionOperation.class );
  
  private String version;
  
  /**
   * @throws MetadataException 
   * 
   */
  public AddVersionOperation(  Object data  )
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
   * add version to the in-memory metadata instance
   * 
   * @param metadata
   * @param version
   * @return
   * @throws MetadataException 
   */
  public boolean perform( Metadata metadata )
  throws MetadataException
  {
    if( metadata == null )
      return false;
   
    Versioning vs = metadata.getVersioning(); 
    
    if( vs == null )
    {
      vs = new Versioning();
      metadata.setVersioning( vs );
    }
    
    if( vs.getVersions() != null && vs.getVersions().size() > 0 )
    {
      List<String> vl = vs.getVersions();
      if( vl.contains( version ) )
        return false;
    }
    
    vs.addVersion( version );
    vs.setLastUpdated( MetadataBuilder.getUTCTimestamp() );
    
    return true;
  }

}
