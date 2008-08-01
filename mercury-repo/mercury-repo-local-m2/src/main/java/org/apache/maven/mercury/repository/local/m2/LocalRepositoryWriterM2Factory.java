package org.apache.maven.mercury.repository.local.m2;

import org.apache.maven.mercury.repository.api.AbstractRepository;
import org.apache.maven.mercury.repository.api.LocalRepository;
import org.apache.maven.mercury.repository.api.MetadataProcessor;
import org.apache.maven.mercury.repository.api.Repository;
import org.apache.maven.mercury.repository.api.RepositoryException;
import org.apache.maven.mercury.repository.api.RepositoryReader;
import org.apache.maven.mercury.repository.api.RepositoryReaderFactory;
import org.codehaus.plexus.i18n.DefaultLanguage;
import org.codehaus.plexus.i18n.Language;

public class LocalRepositoryWriterM2Factory
implements RepositoryReaderFactory
{
  private static final Language lang = new DefaultLanguage( LocalRepositoryWriterM2Factory.class );
  private static final LocalRepositoryWriterM2Factory factory = new LocalRepositoryWriterM2Factory();
  
  static 
  {
    AbstractRepository.register( AbstractRepository.DEFAULT_REPOSITORY_TYPE, factory  );
  }
  
  public RepositoryReader getReader( Repository repo, MetadataProcessor mdProcessor)
  throws RepositoryException
  {
    if( repo == null || !(repo instanceof LocalRepository) )
      throw new RepositoryException( lang.getMessage( "bad.repository.type", repo == null ? "null" : repo.getClass().getName() ) );
    
    return new LocalRepositoryReaderM2( (LocalRepository)repo, mdProcessor );
  }

}
