package org.apache.maven.mercury.repository.metadata;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.maven.mercury.util.FileUtil;
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
  //-------------------------------------------------------------------------
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
  public void testMergeOperation()
  throws FileNotFoundException, IOException, XmlPullParserException, MetadataException
  {
    File groupMd = new File( testBase, "group-maven-metadata.xml");
    byte [] targetBytes = FileUtil.readRawData( groupMd );

    Metadata source = new Metadata();
    source.setGroupId( "a" );
    source.setArtifactId( "a" );
    source.setVersion( "1.0.0" );
    Versioning v = new Versioning();
    v.addVersion( "1.0.0" );
    v.addVersion( "2.0.0" );
    source.setVersioning( v );
    
    byte [] resBytes = MetadataBuilder.changeMetadata( targetBytes, new MergeOperation( new MetadataOperand(source) ) );
    
    File resFile = new File( testBase, "group-maven-metadata-write.xml");

    FileUtil.writeRawData( resFile, resBytes );
    
     Metadata mmd = MetadataBuilder.read( new FileInputStream(resFile) );

     assertNotNull( mmd );
     assertEquals("a", mmd.getGroupId() );
     assertEquals("a", mmd.getArtifactId() );
     assertEquals("4", mmd.getVersion() );

     assertNotNull( mmd.getVersioning() );
    
     List<String> versions = mmd.getVersioning().getVersions();
    
     assertNotNull( versions );
     assertEquals( 6, versions.size() );
     assertTrue( versions.contains("1") );
     assertTrue( versions.contains("2") );
     assertTrue( versions.contains("3") );
     assertTrue( versions.contains("4") );
     assertTrue( versions.contains("1.0.0") );
     assertTrue( versions.contains("2.0.0") );
  }
  //-------------------------------------------------------------------------
  public void testAddVersionOperation()
  throws FileNotFoundException, IOException, XmlPullParserException, MetadataException
  {
    File groupMd = new File( testBase, "group-maven-metadata.xml");
    byte [] targetBytes = FileUtil.readRawData( groupMd );

    byte [] resBytes = MetadataBuilder.changeMetadata( targetBytes, new AddVersionOperation( new StringOperand("5") ) );
    
    File resFile = new File( testBase, "group-maven-metadata-write.xml");

    FileUtil.writeRawData( resFile, resBytes );
    
     Metadata mmd = MetadataBuilder.read( new FileInputStream(resFile) );

     assertNotNull( mmd );
     assertEquals("a", mmd.getGroupId() );
     assertEquals("a", mmd.getArtifactId() );
     assertEquals("4", mmd.getVersion() );

     assertNotNull( mmd.getVersioning() );
    
     List<String> versions = mmd.getVersioning().getVersions();
    
     assertNotNull( versions );
     assertEquals( 5, versions.size() );
     assertTrue( versions.contains("1") );
     assertTrue( versions.contains("2") );
     assertTrue( versions.contains("3") );
     assertTrue( versions.contains("4") );
     assertTrue( versions.contains("5") );
  }
  //-------------------------------------------------------------------------
  public void testRemoveVersionOperation()
  throws FileNotFoundException, IOException, XmlPullParserException, MetadataException
  {
    File groupMd = new File( testBase, "group-maven-metadata.xml");
    byte [] targetBytes = FileUtil.readRawData( groupMd );

    byte [] resBytes = MetadataBuilder.changeMetadata( targetBytes, new RemoveVersionOperation( new StringOperand("1") ) );
    
    File resFile = new File( testBase, "group-maven-metadata-write.xml");

    FileUtil.writeRawData( resFile, resBytes );
    
     Metadata mmd = MetadataBuilder.read( new FileInputStream(resFile) );

     assertNotNull( mmd );
     assertEquals("a", mmd.getGroupId() );
     assertEquals("a", mmd.getArtifactId() );
     assertEquals("4", mmd.getVersion() );

     assertNotNull( mmd.getVersioning() );
    
     List<String> versions = mmd.getVersioning().getVersions();
    
     assertNotNull( versions );
     assertEquals( 3, versions.size() );
     assertTrue( !versions.contains("1") );
     assertTrue( versions.contains("2") );
     assertTrue( versions.contains("3") );
     assertTrue( versions.contains("4") );
  }
  //-------------------------------------------------------------------------
  public void testSetSnapshotOperation()
  throws FileNotFoundException, IOException, XmlPullParserException, MetadataException
  {
    File groupMd = new File( testBase, "group-maven-metadata.xml");
    byte [] targetBytes = FileUtil.readRawData( groupMd );
    
    Snapshot sn = new Snapshot();
    sn.setLocalCopy( false );
    sn.setBuildNumber( 35 );
    String ts = MetadataBuilder.getUTCTimestamp();
    sn.setTimestamp( ts );

    byte [] resBytes = MetadataBuilder.changeMetadata( targetBytes, new SetSnapshotOperation( new SnapshotOperand(sn) ) );
    
    File resFile = new File( testBase, "group-maven-metadata-write.xml");

    FileUtil.writeRawData( resFile, resBytes );
    
     Metadata mmd = MetadataBuilder.read( new FileInputStream(resFile) );

     assertNotNull( mmd );
     assertEquals("a", mmd.getGroupId() );
     assertEquals("a", mmd.getArtifactId() );
     assertEquals("4", mmd.getVersion() );

     assertNotNull( mmd.getVersioning() );
     Snapshot snapshot = mmd.getVersioning().getSnapshot();
     assertNotNull( snapshot );
     assertEquals( ts, snapshot.getTimestamp() );
     
     // now let's drop sn
     targetBytes = FileUtil.readRawData( resFile );
     resBytes = MetadataBuilder.changeMetadata( targetBytes, new SetSnapshotOperation( new SnapshotOperand(null) ) );
     
     Metadata mmd2 = MetadataBuilder.read( new ByteArrayInputStream(resBytes) );

     assertNotNull( mmd2 );
     assertEquals("a", mmd2.getGroupId() );
     assertEquals("a", mmd2.getArtifactId() );
     assertEquals("4", mmd2.getVersion() );

     assertNotNull( mmd2.getVersioning() );
     
     snapshot = mmd2.getVersioning().getSnapshot();
     assertNull( snapshot );
  }
  //-------------------------------------------------------------------------
  public void testMultipleOperations()
  throws FileNotFoundException, IOException, XmlPullParserException, MetadataException
  {
    File groupMd = new File( testBase, "group-maven-metadata.xml");
    byte [] targetBytes = FileUtil.readRawData( groupMd );

    ArrayList<MetadataOperation> ops = new ArrayList<MetadataOperation>(2);
    ops.add( new RemoveVersionOperation( new StringOperand("1") ) );
    ops.add( new AddVersionOperation( new StringOperand("8") ) );
    
    byte [] resBytes = MetadataBuilder.changeMetadata( targetBytes, ops  );
    
    File resFile = new File( testBase, "group-maven-metadata-write.xml");

    FileUtil.writeRawData( resFile, resBytes );
    
     Metadata mmd = MetadataBuilder.read( new FileInputStream(resFile) );

     assertNotNull( mmd );
     assertEquals("a", mmd.getGroupId() );
     assertEquals("a", mmd.getArtifactId() );
     assertEquals("4", mmd.getVersion() );

     assertNotNull( mmd.getVersioning() );
    
     List<String> versions = mmd.getVersioning().getVersions();
    
     assertNotNull( versions );
     assertEquals( 4, versions.size() );
     assertTrue( !versions.contains("1") );
     assertTrue( versions.contains("2") );
     assertTrue( versions.contains("3") );
     assertTrue( versions.contains("4") );
     assertTrue( versions.contains("8") );
  }
  //-------------------------------------------------------------------------
  //-------------------------------------------------------------------------
}
