package org.apache.maven.mercury.repository.metadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import junit.framework.TestCase;

import org.apache.maven.mercury.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.apache.maven.mercury.spi.http.client.Binding;
import org.apache.maven.mercury.spi.http.client.retrieve.DefaultRetrievalRequest;
import org.apache.maven.mercury.spi.http.client.retrieve.DefaultRetriever;
import org.apache.maven.mercury.spi.http.client.retrieve.RetrievalResponse;
import org.apache.maven.mercury.spi.http.server.HttpTestServer;
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
  public void testReadRemoteMd()
  throws FileNotFoundException, IOException, XmlPullParserException
  {
    File temp = File.createTempFile("maven", "metadata" );
    HashSet<Binding> bindings = new HashSet<Binding>();
    
    Binding aaMdBinding = new Binding( "http://localhost:"+_port+"/repo/a/a/maven-metadata.xml", temp, true );
    bindings.add( aaMdBinding );
    
    _request.setBindings(bindings);
    
    RetrievalResponse response = _retriever.retrieve(_request);
    
    if( response.hasExceptions() )
      fail("retrieval exceptions: "+response.getExceptions() );
    
    Metadata mmd = _reader.read( new FileInputStream( temp ) );
    temp.delete();
    
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
