package org.apache.maven.mercury.util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
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
  public static final String SYSTEM_PARAMETER_SKIP_NIO_TESTS = "maven.mercury.tests.skip.nio";
  boolean skipNioTests = Boolean.parseBoolean( System.getProperty( SYSTEM_PARAMETER_SKIP_NIO_TESTS, "true" ) );
  
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
  private static void say( String msg )
  {
    System.out.println(msg);
    System.out.flush();
  }
  //----------------------------------------------------------------------------------------
  public void testLock()
  throws Exception
  {
    Ok th1ok = new Ok();
    Ok th2ok = new Ok();
    
    class TestThread1
    extends Thread
    {
      FileLockBundle lock;
      String dir;
      Ok ok;

      public TestThread1( String dir, Ok ok )
      {
        this.dir = dir;
        this.ok = ok;
      }
      @Override
      public void run()
      {
        try
        {
          lock = FileUtil.lockDir( dir, 10L, 10L );
          assertNotNull( lock );
          say("Thread1: lock "+dir+" obtained");
          
          try { sleep( 2000L ); } catch( InterruptedException e ) {}
          say("Thread1: slept for 2s");
          
          lock.release();
          say("Thread1: lock "+dir+" released");
          
          ok.ok();
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
      FileLockBundle lock;
      String dir;
      Ok ok;

      public TestThread2( String dir, Ok ok )
      {
        this.dir = dir;
        this.ok = ok;
      }
      @Override
      public void run()
      {
        try
        {
          sleep(10l);
          lock = FileUtil.lockDir( dir, 10L, 10L );
          assertNull( lock );
          say("Thread2: resource "+dir+" locked");
          
          lock = FileUtil.lockDir( dir, 5000L, 100L );
          assertNotNull( lock );
          
          lock.release();
          say("Thread2: lock "+dir+" released");
          
          ok.ok();
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
    
    TestThread1 th1 = new TestThread1( dirName, th1ok );
    TestThread2 th2 = new TestThread2( dirName, th2ok );
    
    th1.start();
    th2.start();
    
    for(;;)
      if( th1.isAlive() || th2.isAlive() )
        Thread.sleep( 1000L );
      else
        break;
    
    
    assertTrue( th1ok.isOk() );
    
    assertTrue( th2ok.isOk() );
    
    say("Multi-threaded test finished successfully");
  }
  //----------------------------------------------------------------------------------------
  // TODO: 2008-10-06 Oleg: enable if switching to NIO locking between processes
  public void notestLockNio()
  throws Exception
  {
    Ok th1ok = new Ok();
    Ok th2ok = new Ok();
    
    class TestThread1
    extends Thread
    {
      FileLockBundle lock;
      String dir;
      Ok ok;

      public TestThread1( String dir, Ok ok )
      {
        this.dir = dir;
        this.ok = ok;
      }
      @Override
      public void run()
      {
        try
        {
          lock = FileUtil.lockDirNio( dir, 10L, 10L );
          say("NioThread1: got lock "+lock+" on "+dir+" obtained");

          assertNotNull( lock );
          say("NioThread1: lock "+dir+" obtained");
          
          try { sleep( 2000L ); } catch( InterruptedException e ) {}
          say("NioThread1: slept for 2s");
          
          lock.release();
          say("NioThread1: lock "+dir+" released");
          
          ok.ok();
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
      FileLockBundle lock;
      String dir;
      Ok ok;

      public TestThread2( String dir, Ok ok )
      {
        this.dir = dir;
        this.ok = ok;
      }
      @Override
      public void run()
      {
        try
        {
          sleep(10l);
          lock = FileUtil.lockDirNio( dir, 10L, 10L );
          say("NioThread2: got lock "+lock+" on "+dir+" obtained");

          assertNull( lock );
          
          System.out.println("NioThread2: resource "+dir+" busy");
          System.out.flush();
          
          lock = FileUtil.lockDirNio( dir, 5000L, 100L );
          assertNotNull( lock );
          
          say("NioThread2: lock "+dir+" obtained");
          
          lock.release();
          say("NioThread2: lock "+dir+" released");
          
          ok.ok();
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
    
    TestThread1 th1 = new TestThread1( dirName, th1ok );
    TestThread2 th2 = new TestThread2( dirName, th2ok );
    
    th1.start();
    th2.start();
    
    for(;;)
      if( th1.isAlive() || th2.isAlive() )
        Thread.sleep( 1000L );
      else
        break;

if(skipNioTests)
  return;

    assertTrue( th1ok.isOk() );
    
    assertTrue( th2ok.isOk() );
    
    say("Multi-threaded NIO test finished successfully");
  }
}


class Ok
{
  boolean ok = false;
  
  public void ok()
  {
    ok = true;
  }
  
  public boolean isOk()
  {
    return ok;
  }
}
