package org.apache.maven.mercury.repository.cache.fs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.mercury.artifact.ArtifactCoordinates;
import org.apache.maven.mercury.repository.api.RepositoryGAMetadata;
import org.apache.maven.mercury.repository.metadata.Metadata;
import org.apache.maven.mercury.repository.metadata.MetadataBuilder;
import org.apache.maven.mercury.util.FileUtil;

import junit.framework.TestCase;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class CachedGAVTest
    extends TestCase
{
  
  byte [] mdBytes;
  
  CachedGAVMetadata gam;
  
  Metadata omd;

  @Override
  protected void setUp()
  throws Exception
  {
    InputStream is = CachedGAVTest.class.getResourceAsStream( "/gav-metadata.xml" );
    
    mdBytes = FileUtil.readRawData( is );
    
    omd = MetadataBuilder.getMetadata( mdBytes );
    
    gam = new CachedGAVMetadata( mdBytes );
  }
  
  public void testData()
  throws Exception
  {
    assertEquals( omd.getGroupId(), gam.getGAV().getGroupId() );
    assertEquals( omd.getArtifactId(), gam.getGAV().getArtifactId() );
    assertEquals( omd.getVersion(), gam.getGAV().getVersion() );
    
    assertEquals( omd.getVersioning().getVersions().size(), gam.getSnapshots().size() );
  }
  
  public void testRead()
  throws Exception
  {
    File mf = File.createTempFile( "test-ga-", ".xml", new File("./target") );
    gam.cm.save( mf );
    
    CachedGAVMetadata gam2 = new CachedGAVMetadata( mf );

    assertEquals( omd.getGroupId(), gam2.getGAV().getGroupId() );
    assertEquals( omd.getArtifactId(), gam2.getGAV().getArtifactId() );
    assertEquals( omd.getVersion(), gam2.getGAV().getVersion() );
    
    assertEquals( omd.getVersioning().getVersions().size(), gam2.getSnapshots().size() );
  }
  
}
