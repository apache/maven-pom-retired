package org.apache.maven.mercury.repository.metadata;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.maven.mercury.artifact.Artifact;
import org.apache.maven.mercury.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.apache.maven.mercury.repository.metadata.io.xpp3.MetadataXpp3Writer;

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
  
  /**
   * serialize metadata into xml
   * 
   * @param metadata to serialize
   * @param out output to this stream
   * @return same metadata as was passed in
   * @throws MetadataException if any problems occured
   */
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

  /**
   * apply a list of operators to the specified serialized Metadata object
   * 
   * @param metadataBytes - serialized Metadata object
   * @param mutators - operators
   * @return changed serialized object
   * @throws MetadataException
   */
  public static byte [] changeMetadata( byte [] metadataBytes, List<MetadataOperation> mutators )
  throws MetadataException
  {
    if( mutators == null || mutators.size() < 1 )
      return metadataBytes;
    
    Metadata metadata;
    if( metadataBytes == null || metadataBytes.length < 10 )
    {
      metadata = new Metadata();
    }
    else
    {
      ByteArrayInputStream in = new ByteArrayInputStream( metadataBytes );
      metadata = read( in );
    }
    
    for( MetadataOperation op : mutators )
    {
      boolean changed = op.perform( metadata );
    }
    
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    write( metadata, out );
    
    byte [] res = out.toByteArray(); 
    
    return res; 
  }
  /**
   * apply a list of operators to the specified serialized Metadata object
   * 
   * @param metadataBytes - serialized Metadata object
   * @param mutators - operators
   * @return changed serialized object
   * @throws MetadataException
   */
  public static byte [] changeMetadata( Metadata metadata, List<MetadataOperation> mutators )
  throws MetadataException
  {
    
    if( metadata == null )
    {
      metadata = new Metadata();
    }

    if( mutators != null && mutators.size() > 0 )
      for( MetadataOperation op : mutators )
      {
        boolean changed = op.perform( metadata );
      }
    
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    write( metadata, out );
    
    byte [] res = out.toByteArray(); 
    
    return res; 
  }

  public static byte [] changeMetadata( byte [] metadataBytes, MetadataOperation op )
  throws MetadataException
  {
    ArrayList<MetadataOperation> ops = new ArrayList<MetadataOperation>(1);
    ops.add( op );
    
    return changeMetadata( metadataBytes, ops );
  }

  public static byte [] changeMetadata( Metadata metadata, MetadataOperation op )
  throws MetadataException
  {
    ArrayList<MetadataOperation> ops = new ArrayList<MetadataOperation>(1);
    ops.add( op );
    
    return changeMetadata( metadata, ops );
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
  
  public static Snapshot createSnapshot( String version )
  {
    Snapshot sn = new Snapshot();
    
    if( version == null || version.length() < 3 )
      return sn;
    
    String utc = MetadataBuilder.getUTCTimestamp();
    sn.setTimestamp( utc );
    
    if( version.endsWith( Artifact.SNAPSHOT_VERSION ))
      return sn;
    
    String sbn = version.substring( version.lastIndexOf( '-' )+1 );
    int    bn = Integer.parseInt( sbn ); 
    sn.setBuildNumber( bn );
    
    return sn;
  }
  
}
