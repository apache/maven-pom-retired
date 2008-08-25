package org.apache.maven.mercury.util;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import org.apache.maven.mercury.crypto.api.StreamObserverException;
import org.apache.maven.mercury.crypto.api.StreamVerifierAttributes;
import org.apache.maven.mercury.crypto.api.StreamVerifierException;
import org.apache.maven.mercury.crypto.api.StreamVerifierFactory;
import org.apache.maven.mercury.crypto.pgp.PgpStreamVerifierFactory;
import org.apache.maven.mercury.crypto.sha.SHA1VerifierFactory;
import org.codehaus.plexus.i18n.DefaultLanguage;
import org.codehaus.plexus.i18n.Language;

import junit.framework.TestCase;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class FileUtilTest
    extends TestCase
{
  private static final String publicKeyFile = "/pgp/pubring.gpg";
  private static final String secretKeyFile = "/pgp/secring.gpg";
  private static final String keyId         = "0EDB5D91141BC4F2";
  private static final String secretKeyPass = "testKey82";

  private File testDir;
  private File a;
  private File b;
  private File bAsc;
  private File badAsc;

  HashSet<StreamVerifierFactory> vFacs;
  //----------------------------------------------------------------------------------------
  @Override
  protected void setUp()
      throws Exception
  {
    testDir = new File("./target/test-classes");
    a = new File( testDir, "a.jar" );
    b = new File( testDir, "b.jar" );
    bAsc = new File( testDir, "b.jar.asc" );
    badAsc = new File( testDir, "bad.asc" );
    
    FileUtil.copy( a, b, true );
    
    vFacs = new HashSet<StreamVerifierFactory>(2);
    
    tearDown();
  }
  //----------------------------------------------------------------------------------------
  @Override
  protected void tearDown()
      throws Exception
  {
    bAsc.delete();
  }
  //----------------------------------------------------------------------------------------
  private void setPgp( boolean generator )
  throws StreamVerifierException
  {
    if( generator )
      vFacs.add( 
          new PgpStreamVerifierFactory(
              new StreamVerifierAttributes( PgpStreamVerifierFactory.DEFAULT_EXTENSION, false, true )
              , getClass().getResourceAsStream( secretKeyFile )
              , keyId
              , secretKeyPass
                                      )
              );
    else
      vFacs.add( 
          new PgpStreamVerifierFactory(
              new StreamVerifierAttributes( PgpStreamVerifierFactory.DEFAULT_EXTENSION, false, true )
              , getClass().getResourceAsStream( publicKeyFile )
                                      )
              );
    
  }
  //----------------------------------------------------------------------------------------
  private void setSha1( boolean generator )
  {
    vFacs.add( new SHA1VerifierFactory(false,false) );
  }
  //----------------------------------------------------------------------------------------
  public void testVerifyGood()
  throws IOException, StreamObserverException
  {
    setPgp( false );
    FileUtil.verify( a, vFacs, false, true ); 
  }
  //----------------------------------------------------------------------------------------
  public void testVerifyBad()
  throws IOException, StreamObserverException
  {
    setPgp( false );
    FileUtil.copy( badAsc, bAsc, true );

    try
    {
      FileUtil.verify( b, vFacs, false, false ); 
    }
    catch( StreamObserverException e )
    {
      System.out.println( "Caught expected exception: "+e.getMessage() );
      return;
    }
    fail( "Expected exception never thrown:"+StreamObserverException.class.getName() );
  }
  //----------------------------------------------------------------------------------------
  public void testVerifyNoSigNoForce()
  throws IOException, StreamObserverException
  {
    setPgp( false );
    FileUtil.verify( b, vFacs, false, false ); 
  }
  //----------------------------------------------------------------------------------------
  public void testVerifyNoSigForce()
  throws IOException, StreamVerifierException
  {
    setPgp( false );
    
    try
    {
      FileUtil.verify( b, vFacs, false, true );
    }
    catch( StreamObserverException e )
    {
      System.out.println( "Caught expected exception: "+e.getMessage() );
      return;
    }
    fail( "Expected exception never thrown:"+StreamObserverException.class.getName() );
  }
  //----------------------------------------------------------------------------------------
  public void testSign()
  throws IOException, StreamObserverException
  {
    setPgp( true );
    FileUtil.sign( b, vFacs, false, true );

    vFacs.clear();
    setPgp( false );
    FileUtil.verify( b, vFacs, false, true );
  }
  //----------------------------------------------------------------------------------------
}
