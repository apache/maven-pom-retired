package org.apache.maven.mercury.repository.metadata;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;

import junit.framework.TestCase;

import org.apache.maven.mercury.builder.api.MetadataProcessingException;
import org.apache.maven.mercury.repository.MetadataProcessorMock;
import org.apache.maven.mercury.repository.api.RepositoryException;
import org.apache.maven.mercury.repository.api.RepositoryReader;
import org.apache.maven.mercury.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.apache.maven.mercury.repository.remote.m2.RemoteRepositoryM2;
import org.apache.maven.mercury.spi.http.client.retrieve.DefaultRetrievalRequest;
import org.apache.maven.mercury.spi.http.client.retrieve.DefaultRetriever;
import org.apache.maven.mercury.spi.http.client.retrieve.RetrievalResponse;
import org.apache.maven.mercury.spi.http.server.HttpTestServer;
import org.apache.maven.mercury.transport.api.Binding;
import org.apache.maven.mercury.transport.api.Server;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

public class MetadataReaderTest
extends TestCase
{
  MetadataXpp3Reader _reader;
  File _testBase = new File("./target/test-classes/controlledRepo");
  DefaultRetriever _retriever;
  public String _port;
  HttpTestServer _server;
  DefaultRetrievalRequest _request;

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
  }

  protected void tearDown()
  throws Exception
  {
      super.tearDown();
      _server.stop();
      _server.destroy();
  }
  //-------------------------------------------------------------------------
  public void testReadMd()
  throws FileNotFoundException, IOException, XmlPullParserException
  {
     Metadata mmd = _reader.read( new FileInputStream( new File( _testBase, "a/a/maven-metadata.xml") ) );
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
    Server server = new Server( "test", new URL("http://localhost:"+_port+"/repo") );

    RemoteRepositoryM2 rrm2 = new RemoteRepositoryM2( "testRepo", server );
    
    RepositoryReader reader = rrm2.getReader( new MetadataProcessorMock() );
    
    byte [] mmBuf = reader.readRawData( "a/a/maven-metadata.xml" );
    
    assertNotNull( mmBuf );
    assertTrue( mmBuf.length > 1 );
    
    Metadata mmd = _reader.read( new ByteArrayInputStream( mmBuf ) );
    
    validateMmd( mmd );
    
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
  //-------------------------------------------------------------------------
}
