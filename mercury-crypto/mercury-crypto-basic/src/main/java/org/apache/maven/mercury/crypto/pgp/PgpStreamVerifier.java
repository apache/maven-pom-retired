package org.apache.maven.mercury.crypto.pgp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.SignatureException;

import org.apache.maven.mercury.crypto.api.AbstractStreamVerifier;
import org.apache.maven.mercury.crypto.api.StreamObserverException;
import org.apache.maven.mercury.crypto.api.StreamVerifier;
import org.apache.maven.mercury.crypto.api.StreamVerifierAttributes;
import org.apache.maven.mercury.crypto.api.StreamVerifierException;
import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.BCPGOutputStream;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureGenerator;
import org.codehaus.plexus.i18n.DefaultLanguage;
import org.codehaus.plexus.i18n.Language;

/**
 * lightweight pgp stream encoder, created one per stream
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class PgpStreamVerifier
extends AbstractStreamVerifier
implements StreamVerifier
{
  private static final Language lang = new DefaultLanguage( PgpStreamVerifier.class );
  
  private PGPPublicKeyRingCollection trustedPublicKeyRing;

  private PGPSignatureGenerator signatureGenerator;

  private PGPSignature signature;
  
  private String signatureString;
  
  private long length = -1;
  
  private String lastModified;
  
  //-----------------------------------------------------------------------------------
  public PgpStreamVerifier( StreamVerifierAttributes attributes )
  {
    super( attributes );
  }
  //-----------------------------------------------------------------------------------
  public void init( PGPPublicKeyRingCollection trustedPublicKeyRing )
  {
    this.trustedPublicKeyRing = trustedPublicKeyRing;
  }
  //-----------------------------------------------------------------------------------
  public void init( PGPPrivateKey privateKey, int algorithm, int digestAlgorithm )
  throws StreamVerifierException
  {
    if( privateKey == null )
      throw new IllegalArgumentException( lang.getMessage( "null.private.key" ));
    
    try
    {
      signatureGenerator = new PGPSignatureGenerator(   algorithm
                                                      , digestAlgorithm
                                                      , PgpHelper.PROVIDER
                                                    );
      signatureGenerator.initSign( PGPSignature.BINARY_DOCUMENT, privateKey );
      
      signatureString = null;
    }
    catch( Exception e )
    {
      throw new StreamVerifierException(e);
    }
  }
  
  //-----------------------------------------------------------------------------------
  public void byteReady( int b )
  throws StreamObserverException
  {
    try
    {
      if( signature == null )
      {
        if( signatureGenerator == null )
          throw new StreamVerifierException( lang.getMessage( "no.stream.processor" ) );
        signatureGenerator.update( (byte)b );
      }
      else
        signature.update( (byte)b );
    }
    catch( SignatureException e )
    {
      throw new StreamObserverException(e);
    }
  }

  //-----------------------------------------------------------------------------------
  public void bytesReady( byte[] b, int off, int len )
  throws StreamObserverException
  {
    try
    {
      if( signature == null )
      {
        if( signatureGenerator == null )
          throw new StreamVerifierException( lang.getMessage( "no.stream.processor" ) );
        signatureGenerator.update( b, off, len );
      }
      else
        signature.update( b, off, len );
    }
    catch( SignatureException e )
    {
      throw new StreamObserverException(e);
    }
  }
  //-----------------------------------------------------------------------------------
  public void initSignature( String signatureString )
  throws StreamVerifierException
  {
    try
    {
      if( trustedPublicKeyRing == null )
        throw new StreamVerifierException( lang.getMessage( "no.trusted.ring" ) );

      if( signatureString == null || signatureString.length() < 1 )
        throw new StreamVerifierException( lang.getMessage( "no.signature.string" ) );

      signature = PgpHelper.readSignature( new ByteArrayInputStream( signatureString.getBytes() ) );
      
      if( signature == null )
        throw new StreamVerifierException( "no.signatures.in.stream" );
      
      signature.initVerify( trustedPublicKeyRing.getPublicKey( signature.getKeyID() ), PgpHelper.PROVIDER );
    }
    catch( Exception e )
    {
      throw new StreamVerifierException(e);
    }
    
    if( signature == null )
      throw new StreamVerifierException("no.signatures.in.stream");
    
  }
  //-----------------------------------------------------------------------------------
  public boolean verifySignature()
  throws StreamVerifierException
  {

    if( signature == null )
      throw new StreamVerifierException( lang.getMessage( "null.verify.signature" ));

    try
    {
      boolean res = signature.verify(); 
      return res;
    }
    catch( Exception e )
    {
      throw new StreamVerifierException( e );
    }
  }

  //-----------------------------------------------------------------------------------
  public String getSignature()
  throws StreamVerifierException
  {
    if( signatureString != null )
      return signatureString;
    
    if( signatureGenerator == null )
      throw new StreamVerifierException("bad.verify.signature.state");
    
    BCPGOutputStream signaturePgpBytes = null;
    try
    {
      ByteArrayOutputStream signatureBytes = new ByteArrayOutputStream();
      ArmoredOutputStream aos = new ArmoredOutputStream( signatureBytes );
      signaturePgpBytes = new BCPGOutputStream( aos );
      signatureGenerator.generate().encode( signaturePgpBytes );
      signaturePgpBytes.finish();
      aos.flush();
      aos.close();
      signatureString = signatureBytes.toString();
    }
    catch( Exception e )
    {
      throw new StreamVerifierException( e );
    }
    finally
    {
      if( signaturePgpBytes != null ) try { signaturePgpBytes.close(); } catch( Exception any ) {}
    }
    
    signatureGenerator = null;
    
    return signatureString;
  }
  //-----------------------------------------------------------------------------------
  public long getLength()
  {
      return length;
  }
  //-----------------------------------------------------------------------------------
  public void setLength(long length)
  {
      this.length = length;

  }
  //-----------------------------------------------------------------------------------
  public String getLastModified()
  {
      return lastModified;
  }
  //-----------------------------------------------------------------------------------
  public void setLastModified(String time)
  {
      lastModified = time;

  }
}
