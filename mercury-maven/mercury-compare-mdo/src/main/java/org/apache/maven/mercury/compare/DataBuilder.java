package org.apache.maven.mercury.compare;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.StringTokenizer;

import org.apache.maven.mercury.compare.mdo.Dependencies;
import org.apache.maven.mercury.compare.mdo.io.xpp3.DependenciesXpp3Reader;
import org.apache.maven.mercury.compare.mdo.io.xpp3.DependenciesXpp3Writer;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class DataBuilder
{
  public static Dependencies read( File f )
  throws DataException
  {
    try
    {
      return new DependenciesXpp3Reader().read( new FileInputStream(f) );
    }
    catch( Exception e )
    {
      throw new DataException(e);
    }
  }
  
  public static Dependencies read( InputStream in )
  throws DataException
  {
    try
    {
      return new DependenciesXpp3Reader().read( in );
    }
    catch( Exception e )
    {
      throw new DataException(e);
    }
  }
  
  public static Dependencies getMetadata( byte [] in )
  throws DataException
  {
    if( in == null || in.length < 10 )
      return null;

    try
    {
      return new DependenciesXpp3Reader().read( new ByteArrayInputStream(in) );
    }
    catch( Exception e )
    {
      throw new DataException(e);
    }
  }
  
  public static Dependencies write( Dependencies metadata, OutputStream out )
  throws DataException
  {
    if( metadata == null )
      return metadata;

    try
    {
      new DependenciesXpp3Writer().write( new OutputStreamWriter(out), metadata );
      
      return metadata;
    }
    catch( Exception e )
    {
      throw new DataException(e);
    }
  }
  
  public static Dependencies write( Dependencies metadata, File f )
  throws DataException
  {
    if( metadata == null )
      return metadata;

    try
    {
      new DependenciesXpp3Writer().write( new FileWriter(f), metadata );
      
      return metadata;
    }
    catch( Exception e )
    {
      throw new DataException(e);
    }
  }
  
  public static final File getFile( File dir, String groupId, String artifactId, String version, String type )
  {
    return new File( dir, groupId+"_"+artifactId+"_"+version+"_"+type+".deps" );
  }
  
  public static final void visitDeps( File deps, IDepResolver resolver )
  throws Exception
  {
    if( !deps.exists() )
      throw new Exception( "list file "+deps.getCanonicalPath()+" does not exist" );

    BufferedReader r = new BufferedReader( new FileReader(deps) );
    
    for( String line = r.readLine(); line != null; line = r.readLine() )
    {
      if( line.charAt( 0 ) == '#' )
        continue;
      
      StringTokenizer st = new StringTokenizer( line, " :" );
      
      int count = st.countTokens();
      
      if( count < 3 || count > 4 )
      {
        System.out.println( "Cannot parse line: "+line );
        continue;
      }
      
      int i = 0;
      
      String [] gav = new String[4];
      
      while( st.hasMoreTokens() )
        gav[i++ ] = st.nextToken();
      
      try
      {
        resolver.resolve( gav[0], gav[1], gav[2], count == 4 ? gav[3] : "jar" );
      }
      catch( Exception e )
      {
        e.printStackTrace();
      }
    }
  }
  
  public static final void compareDeps( File deps, IDepResolver resolver )
  throws Exception
  {
    if( !deps.exists() )
      throw new Exception( "list file "+deps.getCanonicalPath()+" does not exist" );

    BufferedReader r = new BufferedReader( new FileReader(deps) );
    
    for( String line = r.readLine(); line != null; line = r.readLine() )
    {
      if( line.charAt( 0 ) == '#' )
        continue;
      
      StringTokenizer st = new StringTokenizer( line, " :" );
      
      int count = st.countTokens();
      
      if( count < 3 || count > 4 )
      {
        System.out.println( "Cannot parse line: "+line );
        continue;
      }
      
      int i = 0;
      
      String [] gav = new String[4];
      
      while( st.hasMoreTokens() )
        gav[i++ ] = st.nextToken();
      
      try
      {
        resolver.visit( gav[0], gav[1], gav[2], count == 4 ? gav[3] : "jar" );
      }
      catch( Exception e )
      {
        e.printStackTrace();
      }
    }
  }
}
