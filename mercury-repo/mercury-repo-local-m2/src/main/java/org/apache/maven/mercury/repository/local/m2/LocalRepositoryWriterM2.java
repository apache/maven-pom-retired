package org.apache.maven.mercury.repository.local.m2;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.mercury.artifact.Artifact;
import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;
import org.apache.maven.mercury.artifact.ArtifactMetadata;
import org.apache.maven.mercury.artifact.DefaultArtifact;
import org.apache.maven.mercury.artifact.version.VersionException;
import org.apache.maven.mercury.artifact.version.VersionRange;
import org.apache.maven.mercury.repository.api.AbstracRepositoryReader;
import org.apache.maven.mercury.repository.api.AbstractRepository;
import org.apache.maven.mercury.repository.api.LocalRepository;
import org.apache.maven.mercury.repository.api.MetadataProcessingException;
import org.apache.maven.mercury.repository.api.MetadataProcessor;
import org.apache.maven.mercury.repository.api.MetadataReader;
import org.apache.maven.mercury.repository.api.Repository;
import org.apache.maven.mercury.repository.api.RepositoryException;
import org.apache.maven.mercury.repository.api.RepositoryOperationResult;
import org.apache.maven.mercury.repository.api.RepositoryReader;
import org.apache.maven.mercury.repository.api.RepositoryWriter;

public class LocalRepositoryWriterM2
implements RepositoryWriter
{
  private static final org.slf4j.Logger _log = org.slf4j.LoggerFactory.getLogger( LocalRepositoryWriterM2.class ); 
  //---------------------------------------------------------------------------------------------------------------
  private static final String [] _protocols = new String [] { "file" };
  
  LocalRepository _repo;
  File _repoDir;
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
  //---------------------------------------------------------------------------------------------------------------
  /* (non-Javadoc)
   * @see org.apache.maven.mercury.repository.api.RepositoryWriter#writeArtifact(java.util.Collection)
   */
  public void writeArtifact(
      Collection<Artifact> artifact )
      throws RepositoryException
  {
    // TODO Auto-generated method stub
    
  }
}
