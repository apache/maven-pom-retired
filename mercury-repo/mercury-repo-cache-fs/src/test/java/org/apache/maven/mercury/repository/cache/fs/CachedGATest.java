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
public class CachedGATest
    extends TestCase
{
  
  byte [] mdBytes;
  
  CachedGAMetadata gam;
  
  Metadata omd;

  @Override
  protected void setUp()
  throws Exception
  {
    InputStream is = CachedGATest.class.getResourceAsStream( "/ga-metadata.xml" );
    
    mdBytes = FileUtil.readRawData( is );
    
    omd = MetadataBuilder.getMetadata( mdBytes );
    
    gam = new CachedGAMetadata( mdBytes );
  }
  
  public void testData()
  throws Exception
  {
    assertEquals( omd.getGroupId(), gam.getGA().getGroupId() );
    assertEquals( omd.getArtifactId(), gam.getGA().getArtifactId() );
    
    assertEquals( omd.getVersioning().getVersions().size(), gam.getVersions().size() );
  }
  
  public void testRead()
  throws Exception
  {
    File mf = File.createTempFile( "test-ga-", ".xml", new File("./target") );
    gam.cm.save( mf );
    
    CachedGAMetadata gam2 = new CachedGAMetadata( mf );

    assertEquals( omd.getGroupId(), gam2.getGA().getGroupId() );
    assertEquals( omd.getArtifactId(), gam2.getGA().getArtifactId() );
    
    assertEquals( omd.getVersioning().getVersions().size(), gam2.getVersions().size() );
  }
  
  
}
