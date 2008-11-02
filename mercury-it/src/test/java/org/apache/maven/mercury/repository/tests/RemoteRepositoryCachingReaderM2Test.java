package org.apache.maven.mercury.repository.tests;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;
import org.apache.maven.mercury.builder.api.MetadataReaderException;
import org.apache.maven.mercury.repository.api.RemoteRepository;
import org.apache.maven.mercury.repository.api.RepositoryException;
import org.apache.maven.mercury.repository.api.RepositoryMetadataCache;
import org.apache.maven.mercury.repository.api.RepositoryUpdateIntervalPolicy;
import org.apache.maven.mercury.repository.api.RepositoryUpdatePolicy;
import org.apache.maven.mercury.repository.local.m2.MetadataProcessorMock;
import org.apache.maven.mercury.repository.metadata.Metadata;
import org.apache.maven.mercury.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.apache.maven.mercury.repository.remote.m2.RemoteRepositoryM2;
import org.apache.maven.mercury.repository.virtual.VirtualRepositoryReader;
import org.apache.maven.mercury.spi.http.client.retrieve.DefaultRetrievalRequest;
import org.apache.maven.mercury.spi.http.client.retrieve.DefaultRetriever;
import org.apache.maven.mercury.spi.http.server.HttpTestServer;
import org.apache.maven.mercury.transport.api.Server;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

public class RemoteRepositoryCachingReaderM2Test
extends AbstractRepositoryReaderM2Test
{
  MetadataXpp3Reader _reader;
  File _testBase = new File("./target/test-classes/repo");
  DefaultRetriever _retriever;
  public String _port;
  HttpTestServer _server;
  DefaultRetrievalRequest _request;

  RepositoryMetadataCache _mdCache;
  File _cacheBase;

  

  //-------------------------------------------------------------------------
  @Override
  protected void setUp()
  throws Exception
  {
    _retriever = new DefaultRetriever();
    _server = new HttpTestServer( _testBase, "/repo" );
    _server.start();
    _port = String.valueOf( _server.getPort() );

    _reader = new MetadataXpp3Reader();
    _request = new DefaultRetrievalRequest();

    mdProcessor = new MetadataProcessorMock();

    query = new ArrayList<ArtifactBasicMetadata>();

    server = new Server( "test", new URL("http://localhost:"+_port+"/repo") );
    
    RepositoryUpdatePolicy up = new RepositoryUpdateIntervalPolicy( RepositoryUpdateIntervalPolicy.UPDATE_POLICY_DAILY );
    
    repo = new RemoteRepositoryM2( "testRepo", server );
    ((RemoteRepository)repo).setUpdatePolicy( up );
    
    repo.setDependencyProcessor(  new MetadataProcessorMock() );
    reader = repo.getReader();

    _cacheBase = new File( _testBase, VirtualRepositoryReader.METADATA_CACHE_DIR );
    _cacheBase.delete();
    _cacheBase.mkdirs();
    
    _mdCache = VirtualRepositoryReader.getCache( _testBase );
    
    reader.setMetadataCache( _mdCache );
    
  }
  //-------------------------------------------------------------------------
  @Override
  protected void tearDown()
  throws Exception
  {
      super.tearDown();
      _server.stop();
      _server.destroy();
  }
  //-------------------------------------------------------------------------
  private void validateMmd( Metadata mmd )
  {
    assertNotNull( mmd );
    assertEquals("a", mmd.getGroupId() );
    assertEquals("a", mmd.getArtifactId() );

    assertNotNull( mmd.getVersioning() );
    
    List<String> versions = mmd.getVersioning().getVersions();
    
    assertNotNull( versions );
    assertTrue( versions.size() > 3 );
  }
  //-------------------------------------------------------------------------
  public void testReadMd()
  throws FileNotFoundException, IOException, XmlPullParserException
  {
      FileInputStream fis = new FileInputStream( new File( _testBase, "a/a/maven-metadata.xml") );
     Metadata mmd = _reader.read( fis );
     fis.close();
     validateMmd( mmd );
     
     File cachedMd = new File( _cacheBase, "a/a/"+"meta-ga-"+repo.getId()+".xml" );
     
     assertTrue( cachedMd.exists() );
  }
  //-------------------------------------------------------------------------
  public void testReadRemoteMdViaRepositoryReader()
  throws FileNotFoundException, IOException, XmlPullParserException, RepositoryException, MetadataReaderException
  {
    
    byte [] mmBuf = reader.readRawData( "a/a/maven-metadata.xml" );
    
    assertNotNull( mmBuf );
    assertTrue( mmBuf.length > 1 );
    
    ByteArrayInputStream bais = new ByteArrayInputStream( mmBuf );
    Metadata mmd = _reader.read( bais );
    bais.close();
    
    validateMmd( mmd );
    
    File cachedMd = new File( _cacheBase, "a/a/"+"meta-ga-"+repo.getId()+".xml" );
    
    assertTrue( cachedMd.exists() );
  }
  //-------------------------------------------------------------------------
  @Override
  public void testReadLatest()
      throws IllegalArgumentException,
      RepositoryException
  {
    super.testReadLatest();

    File cachedMd = new File( _cacheBase, "a/a/"+"meta-ga-"+repo.getId()+".xml" );
    
    assertTrue( cachedMd.exists() );

    File cachedVMd = new File( _cacheBase, "a/a/a-5-SNAPSHOT/"+"meta-gav-"+repo.getId()+".xml" );
    
    assertTrue( cachedVMd.exists() );
  }
  //-------------------------------------------------------------------------
  @Override
  public void testReadRelease()
      throws IllegalArgumentException,
      RepositoryException
  {
    // TODO Auto-generated method stub
    super.testReadRelease();
  }
  //-------------------------------------------------------------------------
  //-------------------------------------------------------------------------
}
