package org.apache.maven.mercury.repository.metadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import junit.framework.TestCase;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class MetadataBuilderTest
    extends TestCase
{
  MetadataBuilder mb;
  File testBase = new File("./target/test-classes/controlledRepo");

  //-------------------------------------------------------------------------
  @Override
  protected void setUp()
  throws Exception
  {
    File temp = new File( testBase, "group-maven-metadata-write.xml");
    if( temp.exists() )
      temp.delete();
  }

  protected void tearDown()
  throws Exception
  {
  }
  //-------------------------------------------------------------------------
  public void testReadGroupMd()
  throws FileNotFoundException, IOException, XmlPullParserException, MetadataException
  {
    File groupMd = new File( testBase, "group-maven-metadata.xml");
     Metadata mmd = MetadataBuilder.read(  new FileInputStream( groupMd ) );

     assertNotNull( mmd );
     assertEquals("a", mmd.getGroupId() );
     assertEquals("a", mmd.getArtifactId() );

     assertNotNull( mmd.getVersioning() );
    
     List<String> versions = mmd.getVersioning().getVersions();
    
     assertNotNull( versions );
     assertEquals( 4, versions.size() );
  }
  //-------------------------------------------------------------------------
  public void testWriteGroupMd()
  throws FileNotFoundException, IOException, XmlPullParserException, MetadataException
  {
    File groupMd = new File( testBase, "group-maven-metadata-write.xml");
    Metadata md = new Metadata();
    md.setGroupId( "a" );
    md.setArtifactId( "a" );
    md.setVersion( "1.0.0" );
    Versioning v = new Versioning();
    v.addVersion( "1.0.0" );
    v.addVersion( "2.0.0" );
    md.setVersioning( v );
    
     MetadataBuilder.write(  md, new FileOutputStream( groupMd ) );
     Metadata mmd = MetadataBuilder.read( new FileInputStream(groupMd) );

     assertNotNull( mmd );
     assertEquals("a", mmd.getGroupId() );
     assertEquals("a", mmd.getArtifactId() );
     assertEquals("1.0.0", mmd.getVersion() );

     assertNotNull( mmd.getVersioning() );
    
     List<String> versions = mmd.getVersioning().getVersions();
    
     assertNotNull( versions );
     assertEquals( 2, versions.size() );
  }
  //-------------------------------------------------------------------------
  //-------------------------------------------------------------------------
}
