package org.apache.maven.mercury.repository.metadata;

import org.apache.maven.mercury.util.TimeUtil;
import org.codehaus.plexus.i18n.DefaultLanguage;
import org.codehaus.plexus.i18n.Language;

/**
 * adds new snapshot to metadata
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class SetSnapshotOperation
    implements MetadataOperation
{
  private static final Language lang = new DefaultLanguage( SetSnapshotOperation.class );
  
  private Snapshot snapshot;
  
  /**
   * @throws MetadataException 
   * 
   */
  public SetSnapshotOperation(  SnapshotOperand data  )
  throws MetadataException
  {
    setOperand( data );
  }
  
  public void setOperand( Object data )
  throws MetadataException
  {
    if( data == null || !(data instanceof SnapshotOperand) )
      throw new MetadataException( lang.getMessage( "bad.operand", "SnapshotOperand", data == null ? "null" : data.getClass().getName() ) );
    
    snapshot = ((SnapshotOperand)data).getOperand();
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
   
    Versioning vs = metadata.getVersioning(); 
    
    if( vs == null )
    {
      vs = new Versioning();
      metadata.setVersioning( vs );
    }
    
    vs.setSnapshot( snapshot );
    vs.setLastUpdated( TimeUtil.getUTCTimestamp() );
    
    return true;
  }

}
