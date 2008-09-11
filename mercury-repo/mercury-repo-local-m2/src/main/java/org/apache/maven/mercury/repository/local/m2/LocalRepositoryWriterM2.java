package org.apache.maven.mercury.repository.local.m2;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.maven.mercury.artifact.Artifact;
import org.apache.maven.mercury.artifact.Quality;
import org.apache.maven.mercury.artifact.version.DefaultArtifactVersion;
import org.apache.maven.mercury.crypto.api.StreamObserverException;
import org.apache.maven.mercury.crypto.api.StreamVerifierFactory;
import org.apache.maven.mercury.repository.api.AbstractRepository;
import org.apache.maven.mercury.repository.api.LocalRepository;
import org.apache.maven.mercury.repository.api.Repository;
import org.apache.maven.mercury.repository.api.RepositoryException;
import org.apache.maven.mercury.repository.api.RepositoryWriter;
import org.apache.maven.mercury.repository.metadata.AddVersionOperation;
import org.apache.maven.mercury.repository.metadata.Metadata;
import org.apache.maven.mercury.repository.metadata.MetadataBuilder;
import org.apache.maven.mercury.repository.metadata.MetadataException;
import org.apache.maven.mercury.repository.metadata.MetadataOperation;
import org.apache.maven.mercury.repository.metadata.SetSnapshotOperation;
import org.apache.maven.mercury.repository.metadata.Snapshot;
import org.apache.maven.mercury.repository.metadata.SnapshotOperand;
import org.apache.maven.mercury.repository.metadata.StringOperand;
import org.apache.maven.mercury.transport.api.Server;
import org.apache.maven.mercury.util.FileLockBundle;
import org.apache.maven.mercury.util.FileUtil;
import org.codehaus.plexus.i18n.DefaultLanguage;
import org.codehaus.plexus.i18n.Language;

public class LocalRepositoryWriterM2
extends Thread
implements RepositoryWriter
{
  public static final String SYSTEM_PROPERTY_PARALLEL_WORKERS = "mercury.local.repo.workers";
  public static final int  PARALLEL_WORKERS = Integer.parseInt( System.getProperty( SYSTEM_PROPERTY_PARALLEL_WORKERS, "4" ) );
  
  public static final long SLEEP_FOR_WORKERS_TICK = 20l;

  public static final String SYSTEM_PROPERTY_SLEEP_FOR_LOCK = "mercury.local.lock.wait.millis";
  public static final long SLEEP_FOR_LOCK = Long.parseLong(  System.getProperty( SYSTEM_PROPERTY_SLEEP_FOR_LOCK, "5000" ) );
  
  public static final long SLEEP_FOR_LOCK_TICK = 5l;

  private static final org.slf4j.Logger _log = org.slf4j.LoggerFactory.getLogger( LocalRepositoryWriterM2.class ); 
  private static final Language _lang = new DefaultLanguage( LocalRepositoryReaderM2.class );
  //---------------------------------------------------------------------------------------------------------------
  private static final String [] _protocols = new String [] { "file" };
  
  private final LocalRepository _repo;
  private final File _repoDir;
  private final ArtifactQueue _aq;

  private static final ArifactWriteData LAST_ARTIFACT = new ArifactWriteData( null, null );
  //---------------------------------------------------------------------------------------------------------------
  public LocalRepositoryWriterM2( LocalRepository repo )
  {
    if( repo == null )
      throw new IllegalArgumentException("localRepo cannot be null");
    
    _repoDir = repo.getDirectory();
    if( _repoDir == null )
      throw new IllegalArgumentException("localRepo directory cannot be null");
    
    if( !_repoDir.exists() )
      throw new IllegalArgumentException("localRepo directory \""+_repoDir.getAbsolutePath()+"\" should exist");

    _repo = repo;
    _aq = null;
  }
  //---------------------------------------------------------------------------------------------------------------
  private LocalRepositoryWriterM2( LocalRepository repo, File repoDir, ArtifactQueue aq )
  {
    _repo = repo;
    _repoDir = repoDir;
    _aq = aq;
  }
  //---------------------------------------------------------------------------------------------------------------
  public Repository getRepository()
  {
    return _repo;
  }
  //---------------------------------------------------------------------------------------------------------------
  public boolean canHandle( String protocol )
  {
    return AbstractRepository.DEFAULT_LOCAL_READ_PROTOCOL.equals( protocol );
  }
  //---------------------------------------------------------------------------------------------------------------
  public String[] getProtocols()
  {
    return _protocols;
  }
  //---------------------------------------------------------------------------------------------------------------
  public void close()
  {
  }
  //---------------------------------------------------------------------------------------------------------------
  public void writeArtifact( Collection<Artifact> artifacts )
      throws RepositoryException
  {
    if( artifacts == null || artifacts.size() < 1 )
      return;
    
    int nWorkers = PARALLEL_WORKERS;
    if( artifacts.size() < nWorkers )
      nWorkers = artifacts.size();
    
    ArtifactQueue aq = new ArtifactQueue();
    LocalRepositoryWriterM2 [] workers = new LocalRepositoryWriterM2[ nWorkers ];
      
    for( int i=0; i<nWorkers; i++ )
    {
      workers[ i ] = new LocalRepositoryWriterM2( _repo, _repoDir, aq );
      workers[ i ].start();
    }
    
    for( Artifact artifact : artifacts )
    {
      Set<StreamVerifierFactory> vFacs = null;
      Server server = _repo.getServer();
      if( server != null && server.hasWriterStreamVerifierFactories() )
        vFacs = server.getWriterStreamVerifierFactories();
      
      if( vFacs == null ) // let it be empty, but not null
        vFacs = new HashSet<StreamVerifierFactory>(1);

      aq.addArtifact( new ArifactWriteData( artifact, vFacs ) );
    }
    aq.addArtifact( LAST_ARTIFACT );
    
    boolean alive = true;
    while( alive )
    {
      alive = false;
      for( int i=0; i<nWorkers; i++ )
        if( workers[ i ].isAlive() )
        {
          alive = true;
          try { sleep( SLEEP_FOR_WORKERS_TICK ); } catch( InterruptedException ie ) {}
        }
    }
  }
  //---------------------------------------------------------------------------------------------------------------
  /* (non-Javadoc)
   * @see java.lang.Thread#run()
   */
  @Override
  public void run()
  {
    try
    {
      for(;;)
      {
        ArifactWriteData awd = _aq.getArtifact();

        if( awd == null || awd.artifact == null )
            break;
        
        writeArtifact( awd.artifact, awd.vFacs );
      }
    }
    catch (InterruptedException e)
    {
    }
    catch( RepositoryException e )
    {
      throw new RuntimeException(e);
    }
  }
  //---------------------------------------------------------------------------------------------------------------
  public void writeArtifact( final Artifact artifact, final Set<StreamVerifierFactory> vFacs )
      throws RepositoryException
  {
    if( artifact == null )
      return;
    
    boolean isPom = "pom".equals( artifact.getType() );
    
    byte [] pomBlob = artifact.getPomBlob();
    boolean hasPomBlob = pomBlob != null && pomBlob.length > 0;
    
    InputStream in = artifact.getStream();
    if( in == null )
    {
      File aFile = artifact.getFile();
      if( aFile == null && !isPom )
      {
        throw new RepositoryException( _lang.getMessage( "artifact.no.stream", artifact.toString() ) );
      }

      try
      {
        in = new FileInputStream( aFile );
      }
      catch( FileNotFoundException e )
      {
        if( !isPom )
          throw new RepositoryException( e );
      }
    }
    DefaultArtifactVersion dav = new DefaultArtifactVersion( artifact.getVersion() );
    Quality aq = dav.getQuality();
    boolean isSnapshot = aq.equals( Quality.SNAPSHOT_QUALITY ) || aq.equals( Quality.SNAPSHOT_TS_QUALITY );

    String relGroupPath = artifact.getGroupId().replace( '.', '/' )+"/"+artifact.getArtifactId();
    String versionDirName = isSnapshot ? (dav.getBase()+'-'+Artifact.SNAPSHOT_VERSION) : artifact.getVersion();
    String relVersionPath = relGroupPath + '/' + versionDirName;
    
    String lockDir = null;
    FileLockBundle fLock = null;

    try
    {
      
      if( isPom )
      {
        if( in == null && !hasPomBlob )
          throw new RepositoryException( _lang.getMessage( "pom.artifact.no.stream", artifact.toString() ) );
        
        if( in != null )
        {
          byte [] pomBlobBytes = FileUtil.readRawData( in );
          hasPomBlob = pomBlobBytes != null && pomBlobBytes.length > 0;
          if( hasPomBlob )
            pomBlob = pomBlobBytes;
        }
      }

      // create folders
      lockDir = _repoDir.getAbsolutePath()+'/'+relGroupPath;

      File gav = new File( lockDir );
      gav.mkdirs();

//    haveLock = FileUtil.lockDir( lockDir, SLEEP_FOR_LOCK, SLEEP_FOR_LOCK_TICK );
//    if( !haveLock )
//      throw new RepositoryException( _lang.getMessage( "cannot.lock.gav", lockDir, ""+SLEEP_FOR_LOCK ) );
      fLock = FileUtil.lockDir( lockDir, SLEEP_FOR_LOCK, SLEEP_FOR_LOCK_TICK );
      if( fLock == null )
        throw new RepositoryException( _lang.getMessage( "cannot.lock.gav", lockDir, ""+SLEEP_FOR_LOCK ) );

      String fName = _repoDir.getAbsolutePath()+'/'+relVersionPath+'/'+artifact.getBaseName()+'.'+artifact.getType();
      
      if( !isPom ) // first - take care of the binary
        FileUtil.writeAndSign( fName, in, vFacs );

      // GA metadata
      File mdFile = new File( _repoDir, relGroupPath+'/'+_repo.getMetadataName() );
      updateGAMetadata( mdFile, artifact, versionDirName, aq, vFacs );

      // now - GAV metadata
      mdFile = new File( _repoDir, relVersionPath+'/'+_repo.getMetadataName() );
      updateGAVMetadata( mdFile, artifact, aq, vFacs );

      // if classier - nothing else to do :)
      if( artifact.hasClassifier() )
        return;
      
      if( hasPomBlob )
      {
        FileUtil.writeAndSign( _repoDir.getAbsolutePath()+'/'+relVersionPath
                              +'/'+artifact.getArtifactId()+'-'+artifact.getVersion()+".pom", pomBlob, vFacs
                              );
      }
        
    }
    catch( Exception e )
    {
      throw new RepositoryException( e );
    }
    finally
    {
      if( fLock != null )
        fLock.release();
    }
    
  }
  //---------------------------------------------------------------------------------------------------------------
  private void updateGAMetadata(  final File mdFile
                                , final Artifact artifact
                                , final String version
                                , final Quality aq
                                , final Set<StreamVerifierFactory> vFacs
                              )
  throws MetadataException, IOException, StreamObserverException
  {
    Metadata md = null;
    
    if( mdFile.exists() )
    {
      try
      {
        byte [] mdBytes = FileUtil.readRawData( mdFile );
        
        if( mdBytes == null )
          throw new MetadataException( _lang.getMessage( "file.is.empty", mdFile.getAbsolutePath() ));
        
        md = MetadataBuilder.read( new ByteArrayInputStream(mdBytes) );
      }
      catch( MetadataException e )
      {
        throw e;
      }
    }
    else
    {
      md = new Metadata();
      md.setGroupId( artifact.getGroupId() );
      md.setArtifactId( artifact.getArtifactId() );
    }
    
    MetadataOperation mdOp = new AddVersionOperation( new StringOperand( version ) ); 
    
    byte [] resBytes = MetadataBuilder.changeMetadata( md, mdOp );

    FileUtil.writeAndSign( mdFile.getAbsolutePath(), resBytes, vFacs );
  }
  //---------------------------------------------------------------------------------------------------------------
  private void updateGAVMetadata( final File mdFile
                                , final Artifact artifact
                                , final Quality aq
                                , final Set<StreamVerifierFactory> vFacs
                              )
  throws MetadataException, IOException, StreamObserverException
  {
    Metadata md = null;
    
    if( mdFile.exists() )
    {
      byte [] mdBytes = FileUtil.readRawData( mdFile );
      md = MetadataBuilder.read( new ByteArrayInputStream(mdBytes) );
    }
    else
    {
      md = new Metadata();
      md.setGroupId( artifact.getGroupId() );
      md.setArtifactId( artifact.getArtifactId() );
      md.setVersion( artifact.getVersion() );
    }
    List<MetadataOperation> mdOps = new ArrayList<MetadataOperation>(2);
    
    if( aq.equals( Quality.SNAPSHOT_TS_QUALITY ) )
    {
      Snapshot sn = MetadataBuilder.createSnapshot( artifact.getVersion() );
      sn.setLocalCopy( true );
      mdOps.add( new SetSnapshotOperation( new SnapshotOperand(sn) ) );
    }
    
    mdOps.add( new AddVersionOperation( new StringOperand(artifact.getVersion()) ) ); 
 
System.out.println("added "+artifact.getVersion());
System.out.flush();
    byte [] resBytes = MetadataBuilder.changeMetadata( md, mdOps );
    FileUtil.writeAndSign( mdFile.getAbsolutePath(), resBytes, vFacs );
  }
  //---------------------------------------------------------------------------------------------------------------
  //---------------------------------------------------------------------------------------------------------------
}
//=================================================================================================================
class ArifactWriteData
{
  Artifact artifact;
  Set<StreamVerifierFactory> vFacs;
  
  public ArifactWriteData(Artifact artifact, Set<StreamVerifierFactory> vFacs)
  {
    this.artifact = artifact;
    this.vFacs = vFacs;
  }
}
//=================================================================================================================
class ArtifactQueue
{
  LinkedList<ArifactWriteData> queue = new LinkedList<ArifactWriteData>();
  boolean empty = false;
  
  public synchronized void addArtifact( ArifactWriteData awd )
  {
    queue.addLast( awd );
    empty = false;
    notify();
  }

  public synchronized ArifactWriteData getArtifact()
  throws InterruptedException
  {
    if( empty )
      return null;

    while( queue.isEmpty() )
      wait();
    
    ArifactWriteData res = queue.removeFirst();
    
    if( res.artifact == null )
    {
      empty = true;
      return null;
    }

    return res;
  }
}
//=================================================================================================================
