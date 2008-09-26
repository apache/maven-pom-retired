package org.apache.maven.mercury.repository.metadata;

import org.codehaus.plexus.lang.DefaultLanguage;
import org.codehaus.plexus.lang.Language;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class MergeOperation
    implements MetadataOperation
{
  private static final Language lang = new DefaultLanguage( MergeOperation.class );
  
  Metadata sourceMetadata;
  
  /**
   * @throws MetadataException 
   * 
   */
  public MergeOperation( MetadataOperand data )
  throws MetadataException
  {
    setOperand( data );
  }

  /**
   * merge the supplied operand Metadata into this metadata
   */
  public boolean perform( Metadata targetMetadata )
      throws MetadataException
  {
    boolean changed = false;
    
    if( sourceMetadata == null || targetMetadata == null )
      return false;

    for ( java.util.Iterator i = sourceMetadata.getPlugins().iterator(); i.hasNext(); )
    {
        Plugin plugin = (Plugin) i.next();
        boolean found = false;

        for ( java.util.Iterator it = targetMetadata.getPlugins().iterator(); it.hasNext() && !found; )
        {
            Plugin preExisting = (Plugin) it.next();

            if ( preExisting.getPrefix().equals( plugin.getPrefix() ) )
            {
                found = true;
            }
        }

        if ( !found )
        {
            Plugin mappedPlugin = new Plugin();

            mappedPlugin.setArtifactId( plugin.getArtifactId() );

            mappedPlugin.setPrefix( plugin.getPrefix() );

            mappedPlugin.setName( plugin.getName() );

            targetMetadata.addPlugin( mappedPlugin );

            changed = true;
        }
    }

    Versioning sourceVersioning = sourceMetadata.getVersioning();
    if ( sourceVersioning != null )
    {
        Versioning targetVersioning = targetMetadata.getVersioning();
        if ( targetVersioning == null )
        {
            targetVersioning = new Versioning();
            targetMetadata.setVersioning( targetVersioning );
            changed = true;
        }

        for ( java.util.Iterator i = sourceVersioning.getVersions().iterator(); i.hasNext(); )
        {
            String version = (String) i.next();
            if ( !targetVersioning.getVersions().contains( version ) )
            {
                changed = true;
                targetVersioning.getVersions().add( version );
            }
        }
      
        if ( "null".equals( sourceVersioning.getLastUpdated() ) )
        {
            sourceVersioning.setLastUpdated( null );
        }

        if ( "null".equals( targetVersioning.getLastUpdated() ) )
        {
            targetVersioning.setLastUpdated( null );
        }

        if ( sourceVersioning.getLastUpdated() == null || sourceVersioning.getLastUpdated().length() == 0 )
        {
            // this should only be for historical reasons - we assume local is newer
            sourceVersioning.setLastUpdated( targetVersioning.getLastUpdated() );
        }

        if ( targetVersioning.getLastUpdated() == null || targetVersioning.getLastUpdated().length() == 0 ||
             sourceVersioning.getLastUpdated().compareTo( targetVersioning.getLastUpdated() ) >= 0 )
        {
            changed = true;
            targetVersioning.setLastUpdated( sourceVersioning.getLastUpdated() );

            if ( sourceVersioning.getRelease() != null )
            {
                changed = true;
                targetVersioning.setRelease( sourceVersioning.getRelease() );
            }
            if ( sourceVersioning.getLatest() != null )
            {
                changed = true;
                targetVersioning.setLatest( sourceVersioning.getLatest() );
            }

            Snapshot s = targetVersioning.getSnapshot();
            Snapshot snapshot = sourceVersioning.getSnapshot();
            if ( snapshot != null )
            {
                if ( s == null )
                {
                    s = new Snapshot();
                    targetVersioning.setSnapshot( s );
                    changed = true;
                }

                // overwrite
                if ( s.getTimestamp() == null ? snapshot.getTimestamp() != null
                    : !s.getTimestamp().equals( snapshot.getTimestamp() ) )
                {
                    s.setTimestamp( snapshot.getTimestamp() );
                    changed = true;
                }
                if ( s.getBuildNumber() != snapshot.getBuildNumber() )
                {
                    s.setBuildNumber( snapshot.getBuildNumber() );
                    changed = true;
                }
                if ( s.isLocalCopy() != snapshot.isLocalCopy() )
                {
                    s.setLocalCopy( snapshot.isLocalCopy() );
                    changed = true;
                }
            }
        }
    }
    return changed;
  }

  public void setOperand( Object data )
      throws MetadataException
  {
        if( data == null || !(data instanceof MetadataOperand) )
          throw new MetadataException( lang.getMessage( "bad.operand", "MetadataOperand", data == null ? "null" : data.getClass().getName() ) );
        
        sourceMetadata = ((MetadataOperand)data).getOperand();
  }

}
