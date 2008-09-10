package org.apache.maven.mercury.util;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileLock;
import java.util.HashSet;

import junit.framework.TestCase;

import org.apache.maven.mercury.crypto.api.StreamObserverException;
import org.apache.maven.mercury.crypto.api.StreamVerifierAttributes;
import org.apache.maven.mercury.crypto.api.StreamVerifierException;
import org.apache.maven.mercury.crypto.api.StreamVerifierFactory;
import org.apache.maven.mercury.crypto.pgp.PgpStreamVerifierFactory;
import org.apache.maven.mercury.crypto.sha.SHA1VerifierFactory;

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
  public void testLock()
  throws Exception
  {
    class TestThread1
    extends Thread
    {
      boolean lock;
      String dir;

      public TestThread1( String dir )
      {
        this.dir = dir;
      }
      @Override
      public void run()
      {
        try
        {
          lock = FileUtil.lockDir( dir, 1000L, 100L );
          assertTrue( lock );
          System.out.println("Thread1: lock "+dir+" obtained");
          
          try { sleep( 2000L ); } catch( InterruptedException e ) {}
          System.out.println("Thread1: slept for 2s");
          
          FileUtil.unlockDir( dir );
          System.out.println("Thread1: lock "+dir+" released");
        }
        catch( Exception e )
        {
          fail( e.getMessage() );
        }
      }
      
    }
    
    class TestThread2
    extends Thread
    {
      boolean lock;
      String dir;

      public TestThread2( String dir )
      {
        this.dir = dir;
      }
      @Override
      public void run()
      {
        try
        {
          lock = FileUtil.lockDir( dir, 10L, 10L );
          assertFalse( lock );
          System.out.println("Thread2: resource "+dir+" locked");
          
          lock = FileUtil.lockDir( dir, 5000L, 100L );
          assertNotNull( lock );
          
          FileUtil.unlockDir( dir );
          System.out.println("Thread2: lock "+dir+" released");
        }
        catch( Exception e )
        {
          fail( e.getMessage() );
        }
      }
      
    }
    
    File dir = File.createTempFile( "test-", "-dir" );
    String dirName = dir.getAbsolutePath();
    dir.delete();
    dir = new File( dirName );
    dir.mkdir();
    dir.deleteOnExit();
    
    TestThread1 th1 = new TestThread1( dirName );
    TestThread2 th2 = new TestThread2( dirName );
    
    th1.start();
    th2.start();
    
    for(;;)
      if( th1.isAlive() || th2.isAlive() )
        Thread.sleep( 1000L );
      else
        break;
    
    System.out.println("Multi-threaded test finished successfully");
  }
  //----------------------------------------------------------------------------------------
}
