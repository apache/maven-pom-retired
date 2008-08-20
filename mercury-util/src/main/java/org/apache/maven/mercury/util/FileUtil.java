package org.apache.maven.mercury.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class FileUtil
{
  public static final String DEFAULT_CHARSET = "utf-8";
  //---------------------------------------------------------------------------------------------------------------
  public static void delete( File f )
  {
    if( ! f.exists()  )
      return;
    
    if( f.isDirectory() )
    {
      File [] kids = f.listFiles();
      for( File kid : kids )
        delete( kid );
    }
    
    f.delete();
  }
  //---------------------------------------------------------------------------------------------------------------
  public static void copy( File fromFile, File toFile, boolean clean )
  throws IOException
  {
    if( toFile.exists() && clean )
      delete( toFile );
    
    if( fromFile.isFile() )
    {
      copyFile( fromFile, toFile );
      return;
    }
    
    File [] kids = fromFile.listFiles();
    for( File kid : kids )
    {
      if( kid.isDirectory() )
      {
        File newDir = new File( toFile, kid.getName());
        newDir.mkdirs();
        
        copy( kid, newDir, false );
      }
      else
        copyFile( kid, toFile );
    }
   
  }
  //---------------------------------------------------------------------------------------------------------------
  private static void copyFile( File f, File toDir )
  throws IOException
  {
    File fOut = new File(toDir, f.getName() );
    FileInputStream fis = new FileInputStream(f);
    writeRawData( fOut, fis );
  }
  //---------------------------------------------------------------------------------------------------------------
  public static byte[] readRawData( File file )
  throws IOException
  {
    if( ! file.exists() )
      return null;
    
    FileInputStream fis = null;
    
    try
    {
      fis = new FileInputStream( file );
      int len = (int)file.length();
      byte [] pom = new byte [ len ];
      fis.read( pom );
      return pom;
    }
    catch( IOException e )
    {
      throw  e;
    }
    finally
    {
      if( fis != null ) try { fis.close(); } catch( Exception any ) {}
    }
  }
  //---------------------------------------------------------------------------------------------------------------
  public static byte[] readRawData( InputStream in )
  throws IOException
  {
    byte [] bytes = new byte [ 10240 ];
    int n = -1;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    
    while( (n = in.read( bytes )) != -1 )
      baos.write( bytes, 0, n );
    
    return baos.toByteArray();
  }
  //---------------------------------------------------------------------------------------------------------------
  public static void writeRawData( File file, String sBytes )
  throws IOException
  {
    writeRawData( file, sBytes.getBytes( DEFAULT_CHARSET ) );
  }
  //---------------------------------------------------------------------------------------------------------------
  public static void writeRawData( File f, InputStream in )
  throws IOException
  {
    OutputStream out = new FileOutputStream( f );

    byte [] buf = new byte[ 10240 ];
    int n;

    while( (n = in.read(buf)) > 0 )
        out.write(buf, 0, n);

    in.close();
    out.flush();
    out.close();
  }
  //---------------------------------------------------------------------------------------------------------------
  public static void writeRawData( File file, byte [] bytes )
  throws IOException
  {
    if( file.exists() )
      file.delete();
    
    FileOutputStream fos = null;
    
    try
    {
      fos = new FileOutputStream( file );
      fos.write( bytes );
    }
    catch( IOException e )
    {
      throw  e;
    }
    finally
    {
      if( fos != null ) try { fos.close(); } catch( Exception any ) {}
    }
  }
  //---------------------------------------------------------------------------------------------------------------
  //---------------------------------------------------------------------------------------------------------------
}
