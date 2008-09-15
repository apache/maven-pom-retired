package org.apache.maven.mercury.repository.remote.m2;

import org.apache.maven.mercury.builder.api.DependencyProcessor;
import org.apache.maven.mercury.repository.api.AbstractRepository;
import org.apache.maven.mercury.repository.api.RemoteRepository;
import org.apache.maven.mercury.repository.api.Repository;
import org.apache.maven.mercury.repository.api.RepositoryException;
import org.apache.maven.mercury.repository.api.RepositoryReader;
import org.apache.maven.mercury.repository.api.RepositoryReaderFactory;
import org.codehaus.plexus.i18n.DefaultLanguage;
import org.codehaus.plexus.i18n.Language;

public class RemoteRepositoryReaderM2Factory
implements RepositoryReaderFactory
{
  private static final Language lang = new DefaultLanguage( RemoteRepositoryReaderM2Factory.class );
  private static final RemoteRepositoryReaderM2Factory factory = new RemoteRepositoryReaderM2Factory();
  
  static 
  {
    AbstractRepository.register( AbstractRepository.DEFAULT_REPOSITORY_TYPE, factory  );
  }
  
  public RepositoryReader getReader( Repository repo, DependencyProcessor mdProcessor )
  throws RepositoryException
  {
    if( repo == null || !(repo instanceof RemoteRepository) )
      throw new RepositoryException( lang.getMessage( "bad.repository.type", repo == null ? "null" : repo.getClass().getName() ) );
    
    return new RemoteRepositoryReaderM2( (RemoteRepository)repo, mdProcessor );
  }

}
