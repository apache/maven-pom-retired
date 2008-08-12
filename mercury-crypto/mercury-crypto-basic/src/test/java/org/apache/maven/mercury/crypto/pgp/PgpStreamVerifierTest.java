package org.apache.maven.mercury.crypto.pgp;

import java.io.IOException;
import java.io.InputStream;

import org.apache.maven.mercury.crypto.api.StreamObserverException;
import org.apache.maven.mercury.crypto.api.StreamVerifierAttributes;
import org.apache.maven.mercury.crypto.api.StreamVerifierException;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;

import junit.framework.TestCase;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class PgpStreamVerifierTest
    extends TestCase
{
  private static final String keyId   = "0EDB5D91141BC4F2";

  private static final String secretKeyFile = "/secring.gpg";
  private static final String secretKeyPass = "testKey82";

  private static final String publicKeyFile = "/pubring.gpg";
  
  private PGPSecretKeyRing secretKeyRing;
  private PGPSecretKey secretKey;
  private PGPPublicKey publicKey;
  
  PgpStreamVerifierFactory svf;
  
  PgpStreamVerifier sv;
  

  protected void setUp()
      throws Exception
  {
    InputStream in = getClass().getResourceAsStream( secretKeyFile );
    assertNotNull( in );
    
    secretKeyRing = PgpHelper.readKeyRing( in, keyId );
    assertNotNull( secretKeyRing );
    
    secretKey = secretKeyRing.getSecretKey( Long.parseLong( keyId, 16 ) );
    publicKey = secretKeyRing.getPublicKey();
    
    StreamVerifierAttributes attributes = new StreamVerifierAttributes(PgpStreamVerifierFactory.DEFAULT_EXTENSION, true, true);
    
    InputStream is = getClass().getResourceAsStream( publicKeyFile );
    svf = new PgpStreamVerifierFactory( attributes, is );
    is.close();
    
    is = getClass().getResourceAsStream( secretKeyFile );
    svf.init( is, keyId, secretKeyPass );
    is.close();
  }

  protected void tearDown()
      throws Exception
  {
    super.tearDown();
  }
  //-------------------------------------------------------------------------------------------------
  public void testGenerateSignature()
  throws IOException, StreamObserverException
  {
    PgpStreamVerifier sv = (PgpStreamVerifier)svf.newInstance();
    InputStream in = getClass().getResourceAsStream( "/file.gif" );
    
    int b;
    while( (b = in.read()) != -1 )
      sv.byteReady( b );
    
    String sig = sv.getSignature();
    
    assertNotNull( sig );
    
    assertTrue( sig.length() > 10 );
    
//    System.out.println("Signature is \n"+sig+"\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
  }
  //-------------------------------------------------------------------------------------------------
  public void testVerifySignature()
  throws IOException, StreamObserverException
  {
    PgpStreamVerifier sv = (PgpStreamVerifier)svf.newInstance();

    InputStream in = getClass().getResourceAsStream( "/file.gif" );
    String sig = PgpHelper.streamToString( getClass().getResourceAsStream( "/file.gif.asc" ) );
    
    sv.initSignature( sig );
    
    int b;
    while( (b = in.read()) != -1 )
      sv.byteReady( b );
    
    boolean verified = sv.verifySignature();
    
    assertTrue( verified );
    
    System.out.println("BouncyCastle Signature is "+verified);
  }
  //-------------------------------------------------------------------------------------------------
  public void testVerifyExternalSignature()
  throws IOException, StreamObserverException
  {
    PgpStreamVerifier sv = (PgpStreamVerifier)svf.newInstance();

    InputStream in = getClass().getResourceAsStream( "/file.gif" );
    String sig = PgpHelper.streamToString( getClass().getResourceAsStream( "/file.gif.asc.external" ) );
    
    sv.initSignature( sig );
    
    int b;
    while( (b = in.read()) != -1 )
      sv.byteReady( b );
    
    boolean verified = sv.verifySignature();
    
    assertTrue( verified );
    
    System.out.println("3rd Party Signature is "+verified);
  }
  //-------------------------------------------------------------------------------------------------
}
