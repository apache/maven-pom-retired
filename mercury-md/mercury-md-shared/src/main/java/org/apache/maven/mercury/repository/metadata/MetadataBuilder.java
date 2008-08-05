package org.apache.maven.mercury.repository.metadata;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Date;

import org.apache.maven.mercury.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.apache.maven.mercury.repository.metadata.io.xpp3.MetadataXpp3Writer;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * utility class to help with de/serializing metadata from/to XML
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class MetadataBuilder
{
  /**
   * instantiate Metadata from a stream
   * 
   * @param in
   * @return
   * @throws MetadataException
   */
  public static Metadata read( InputStream in )
  throws MetadataException
  {
    try
    {
      return new MetadataXpp3Reader().read( in );
    }
    catch( Exception e )
    {
      throw new MetadataException(e);
    }
  }
  
  public static Metadata write( Metadata metadata, OutputStream out )
  throws MetadataException
  {
    if( metadata == null )
      return metadata;

    try
    {
      new MetadataXpp3Writer().write( new OutputStreamWriter(out), metadata );
      
      return metadata;
    }
    catch( Exception e )
    {
      throw new MetadataException(e);
    }
  }
  
  public static boolean merge( Metadata sourceMetadata, Metadata targetMetadata )
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

      Versioning versioning = sourceMetadata.getVersioning();
      if ( versioning != null )
      {
          Versioning v = targetMetadata.getVersioning();
          if ( v == null )
          {
              v = new Versioning();
              targetMetadata.setVersioning( v );
              changed = true;
          }

          for ( java.util.Iterator i = versioning.getVersions().iterator(); i.hasNext(); )
          {
              String version = (String) i.next();
              if ( !v.getVersions().contains( version ) )
              {
                  changed = true;
                  v.getVersions().add( version );
              }
          }
        
          if ( "null".equals( versioning.getLastUpdated() ) )
          {
              versioning.setLastUpdated( null );
          }

          if ( "null".equals( v.getLastUpdated() ) )
          {
              v.setLastUpdated( null );
          }

          if ( versioning.getLastUpdated() == null || versioning.getLastUpdated().length() == 0 )
          {
              // this should only be for historical reasons - we assume local is newer
              versioning.setLastUpdated( v.getLastUpdated() );
          }

          if ( v.getLastUpdated() == null || v.getLastUpdated().length() == 0 ||
               versioning.getLastUpdated().compareTo( v.getLastUpdated() ) >= 0 )
          {
              changed = true;
              v.setLastUpdated( versioning.getLastUpdated() );

              if ( versioning.getRelease() != null )
              {
                  changed = true;
                  v.setRelease( versioning.getRelease() );
              }
              if ( versioning.getLatest() != null )
              {
                  changed = true;
                  v.setLatest( versioning.getLatest() );
              }

              Snapshot s = v.getSnapshot();
              Snapshot snapshot = versioning.getSnapshot();
              if ( snapshot != null )
              {
                  if ( s == null )
                  {
                      s = new Snapshot();
                      v.setSnapshot( s );
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
  
  /**
   * update snapshot timestamp to now
   * 
   * @param target
   */
  public static void updateTimestamp( Snapshot target )
  {
      target.setTimestamp( getUTCTimestamp() );
  }
  
  /**
   * update versioning's lastUpdated timestamp to now
   * 
   * @param target
   */
  public static void updateTimestamp( Versioning target )
  {
      target.setLastUpdated( getUTCTimestamp() );
  }
  
  /**
   * 
   * @return current UTC timestamp by yyyyMMddHHmmss mask
   */
  public static String getUTCTimestamp( )
  {
    return getUTCTimestamp( new Date() );
  }

  /**
   * 
   * @param date
   * @return current date converted to UTC timestamp by yyyyMMddHHmmss mask
   */
  public static String getUTCTimestamp( Date date )
  {
      java.util.TimeZone timezone = java.util.TimeZone.getTimeZone( "UTC" );
      java.text.DateFormat fmt = new java.text.SimpleDateFormat( "yyyyMMddHHmmss" );
      fmt.setTimeZone( timezone );
      return fmt.format( date );
  }
  
}
