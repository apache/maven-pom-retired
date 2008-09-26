package org.apache.maven.mercury.crypto.pgp;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Security;
import java.util.Iterator;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPCompressedData;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPObjectFactory;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureList;
import org.bouncycastle.openpgp.PGPUtil;
import org.codehaus.plexus.lang.DefaultLanguage;
import org.codehaus.plexus.lang.Language;

/**
 * PGP helper - collection of utility methods, loosely based on one of the
 * Bouncy Castle's SignedFileProcessor.java
 * 
 */
public class PgpHelper
{
  public static final String PROVIDER = "BC";
  public static final String EXTENSION = "asc";

  private static final Language lang = new DefaultLanguage( PgpHelper.class );
  //---------------------------------------------------------------------------------
  static 
  {
    Security.addProvider( new BouncyCastleProvider() );
  }
  //---------------------------------------------------------------------------------
  /**
   * load a key ring stream and find the secret key by hex id
   * 
   * @param in PGP keystore
   * @param hexId key id
   * @return
   * @throws IOException
   * @throws PGPException
   */
  public static PGPSecretKeyRing readKeyRing( InputStream in, String hexId )
  throws IOException, PGPException
  {
    if( in == null )
      throw new IllegalArgumentException( lang.getMessage( "null.input.stream" ) );

    if( hexId == null || hexId.length() < 16 )
      throw new IllegalArgumentException( lang.getMessage( "bad.key.id", hexId ) );
    
    long id = hexToId( hexId );

    in = PGPUtil.getDecoderStream( in );

    PGPSecretKeyRingCollection pgpSec = new PGPSecretKeyRingCollection( in );

    Iterator<PGPSecretKeyRing> ringIt = pgpSec.getKeyRings();

    while( ringIt.hasNext() )
    {
      PGPSecretKeyRing keyRing = ringIt.next();
      PGPSecretKey key = keyRing.getSecretKey( id );
      if( key != null )
        return keyRing;
    }

    throw new IllegalArgumentException( lang.getMessage( "no.secret.key", hexId ) );
  }
  //---------------------------------------------------------------------------------
  public static long hexToId( String hexId )
  {
    return Long.parseLong( hexId, 16 );
  }
  //---------------------------------------------------------------------------------
  public static PGPSignature readSignature( InputStream inS )
  throws IOException, PGPException
  {
    if( inS == null )
      throw new IllegalArgumentException( "null.input.stream" );
    
    InputStream in = inS;
    in = PGPUtil.getDecoderStream( in );
  
    PGPObjectFactory pgpObjectFactory = new PGPObjectFactory( in );
    PGPSignatureList sigList = null;

    Object pgpObject = pgpObjectFactory.nextObject();
    
    if( pgpObject == null )
      throw new PGPException( lang.getMessage( "no.objects.in.stream" ) );

    if( pgpObject instanceof PGPCompressedData )
    {
        PGPCompressedData cd = (PGPCompressedData)pgpObject;

        pgpObjectFactory = new PGPObjectFactory( cd.getDataStream() );
        
        sigList = (PGPSignatureList)pgpObjectFactory.nextObject();
    }
    else
    {
        sigList = (PGPSignatureList)pgpObject;
    }
    
    if( sigList.size() < 1 )
      throw new PGPException( lang.getMessage( "no.signatures.in.stream" ) );
    
    PGPSignature sig = sigList.get(0);
    
    return sig;
  }
  //---------------------------------------------------------------------------------
  public static String streamToString( InputStream in )
  throws IOException
  {
    if( in == null )
      return null;

    ByteArrayOutputStream ba = new ByteArrayOutputStream();
    int b = 0;
    while( (b = in.read()) != -1 )
      ba.write( b );
    
    return ba.toString();
  }
  //---------------------------------------------------------------------------------
  public static String fileToString( String fileName )
  throws IOException
  {
    FileInputStream fis = null;
    try
    {
      fis = new FileInputStream( fileName );
      return streamToString( fis );
    }
    finally
    {
      if( fis != null ) try { fis.close(); } catch( Exception any ) {}
    }
  }
  //---------------------------------------------------------------------------------
  //---------------------------------------------------------------------------------
}
