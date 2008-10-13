package org.apache.maven.mercury.repository.cache.fs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.maven.mercury.repository.api.MetadataCorruptionException;
import org.apache.maven.mercury.repository.api.RepositoryGAVMetadata;
import org.apache.maven.mercury.repository.cache.md.Attribute;
import org.apache.maven.mercury.repository.cache.md.CachedRawMetadata;
import org.apache.maven.mercury.repository.cache.md.Element;
import org.apache.maven.mercury.repository.cache.md.io.xpp3.CachedMetadataXpp3Reader;
import org.apache.maven.mercury.repository.cache.md.io.xpp3.CachedMetadataXpp3Writer;
import org.apache.maven.mercury.util.Util;
import org.codehaus.plexus.lang.DefaultLanguage;
import org.codehaus.plexus.lang.Language;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * metadata serialization helper - saves/restores element/attribute xml.
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
class CachedMetadata
{
  private static final Language _lang = new DefaultLanguage( CachedMetadata.class );
  
  private CachedRawMetadata crm;
  
  private File mdFile;
  
  private static CachedRawMetadata readRawMetadata( File mdFile )
  throws FileNotFoundException, IOException, XmlPullParserException
  {
    CachedMetadataXpp3Reader reader = new CachedMetadataXpp3Reader();
    return reader.read( new FileReader(mdFile) );
  }
  
  private static void writeRawMetadata( CachedRawMetadata cmd, File mdFile )
  throws FileNotFoundException, IOException, XmlPullParserException
  {
    CachedMetadataXpp3Writer writer = new CachedMetadataXpp3Writer();
    writer.write( new FileWriter(mdFile), cmd );
  }

  protected CachedMetadata()
  {
    crm = new CachedRawMetadata();
  }

  protected CachedMetadata( File mdFile )
  throws FileNotFoundException, IOException, XmlPullParserException
  {
    if( mdFile.exists() )
      crm = readRawMetadata( mdFile );
    else
      crm = new CachedRawMetadata();
    
    this.mdFile = mdFile;
  }

  public void save( File mdFile )
  throws FileNotFoundException, IOException, XmlPullParserException
  {
    writeRawMetadata( crm, mdFile );
  }
  
  protected Element findElement( String elem )
  {
    List<Element> el = crm.getElements();
    if( Util.isEmpty( el ) )
    {
      el = new ArrayList<Element>();
      crm.setElements( el );
    }
    
    Element e = null;
    
    for( Element le : el )
      if( le.getName().equals( elem ) )
      {
        e = le;
        break;
      }
    
    if( e == null )
    {
      e = new Element();
      e.setName( elem );
      el.add( e );
    }
    
    return e;
  }

  protected void cleanAttribute( Element e, String attr  )
  {
    List<Attribute> al = e.getAttributes();

    if( Util.isEmpty( al ))
      return;

    int sz = al.size();
    
    for( int i= (sz-1); i >= 0; i-- )
    {
      Attribute a = al.get( i );
      if( a.getName().equals( attr ) )
        al.remove( i );
    }
  }
  
  protected List<String> findAttributes( Element e, String attr  )
  {
    List<Attribute> al = e.getAttributes();

    if( Util.isEmpty( al ))
    {
      al = new ArrayList<Attribute>();
      e.setAttributes( al );
      return null;
    }
    
    List<String> a = null;
    
    for( Attribute la : al )
      if( la.getName().equals( attr ) )
      {
        if( a == null )
          a = new ArrayList<String>();
        a.add( la.getValue() );
      }
    
    return a;
  }
  
  protected List<String> findAttributes( String elem, String attr  )
  {
    Element e = findElement( elem );
    return findAttributes( e, attr );
  }
  
  protected String getAttribute( String elem, String attr, boolean mandatory  )
  throws MetadataCorruptionException
  {
    Element e = findElement( elem );
    
    List<String> a = findAttributes( e, attr );
    
    if( Util.isEmpty( a ) )
      if( mandatory )
        throw new MetadataCorruptionException(  _lang.getMessage( "no.mandatory.attribute", elem, attr ) );
      else 
        return null;
    
    return a.get( 0 );
  }
  
  protected void addAttribute( Element e, String attr, String val )
  {
    List<Attribute> al = e.getAttributes();
    if( al == null )
    {
      al = new ArrayList<Attribute>();
      e.setAttributes( al );
    }
    
    Attribute a = new Attribute();
    a.setName( attr );
    a.setValue( val );
    
    al.add( a );
  }
  
  protected void setAttribute( String elem, String attr, String val)
  {
    Element e = findElement( elem );
    cleanAttribute( e, attr );
    addAttribute( e, attr, val );
  }

  protected void setAttribute( String elem, String attr, Collection<String> vals )
  {
    Element e = findElement( elem );
    cleanAttribute( e, attr );
    for( String val : vals )
      addAttribute( e, attr, val );
  }
  
  protected void setLastUpdate( String lastUpdated )
  {
    crm.setLastUpdated( lastUpdated );
  }
  
  protected String getLastUpdate()
  {
    return crm.getLastUpdated();
  }
  
  protected void clean()
  {
    crm.setElements( null );
  }

}
