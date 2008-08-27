package org.apache.maven.mercury.repository.tests;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;
import org.apache.maven.mercury.artifact.ArtifactMetadata;
import org.apache.maven.mercury.artifact.QualityRange;
import org.apache.maven.mercury.builder.api.MetadataProcessingException;
import org.apache.maven.mercury.builder.api.MetadataProcessor;
import org.apache.maven.mercury.repository.local.m2.MetadataProcessorMock;
import org.apache.maven.mercury.repository.api.RepositoryException;
import org.apache.maven.mercury.repository.api.AbstractRepOpResult;
import org.apache.maven.mercury.repository.api.RepositoryReader;
import org.apache.maven.mercury.repository.metadata.Metadata;
import org.apache.maven.mercury.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.apache.maven.mercury.repository.remote.m2.RemoteRepositoryM2;
import org.apache.maven.mercury.spi.http.client.retrieve.DefaultRetrievalRequest;
import org.apache.maven.mercury.spi.http.client.retrieve.DefaultRetriever;
import org.apache.maven.mercury.spi.http.client.retrieve.RetrievalResponse;
import org.apache.maven.mercury.spi.http.server.HttpTestServer;
import org.apache.maven.mercury.transport.api.Binding;
import org.apache.maven.mercury.transport.api.Server;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

public class RemoteRepositoryReaderM2Test
extends AbstractRepositoryReaderM2Test
{
  MetadataXpp3Reader _reader;
  File _testBase = new File("./target/test-classes/repo");
  DefaultRetriever _retriever;
  public String _port;
  HttpTestServer _server;
  DefaultRetrievalRequest _request;

  
//  List<ArtifactBasicMetadata> query;
//  MetadataProcessor mdProcessor;
//  ArtifactBasicMetadata bmd;
//
//  Server server;
//  RemoteRepositoryM2 repo;
//  
//  RepositoryReader reader;
//  
//  // setting this to true will add aonatype nexus tests
//  String nexusUrl = System.getProperty( "mercury.nexus.url", null );
//

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
    repo = new RemoteRepositoryM2( "testRepo", server );
    reader = repo.getReader( new MetadataProcessorMock() );
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
  }
  //-------------------------------------------------------------------------
  public void testReadRemoteMdViaHttpClient()
  throws FileNotFoundException, IOException, XmlPullParserException
  {
    File temp = File.createTempFile("maven", "metadata" );
    HashSet<Binding> bindings = new HashSet<Binding>();
    
    Binding aaMdBinding = new Binding( new URL("http://localhost:"+_port+"/repo/a/a/maven-metadata.xml"), temp);
    bindings.add( aaMdBinding );
    
    _request.setBindings(bindings);
    
    RetrievalResponse response = _retriever.retrieve(_request);
    
    if( response.hasExceptions() )
      fail("retrieval exceptions: "+response.getExceptions()+"\nReading from "+aaMdBinding.getRemoteResource() );
    
    Metadata mmd = _reader.read( new FileInputStream( temp ) );
    temp.delete();
    
    validateMmd( mmd );
    
  }
  //-------------------------------------------------------------------------
  public void testReadRemoteMdViaRepositoryReader()
  throws FileNotFoundException, IOException, XmlPullParserException, RepositoryException, MetadataProcessingException
  {
    
    byte [] mmBuf = reader.readRawData( "a/a/maven-metadata.xml" );
    
    assertNotNull( mmBuf );
    assertTrue( mmBuf.length > 1 );
    
    ByteArrayInputStream bais = new ByteArrayInputStream( mmBuf );
    Metadata mmd = _reader.read( bais );
    bais.close();
    
    validateMmd( mmd );
  }
  //-------------------------------------------------------------------------
  //-------------------------------------------------------------------------
}
