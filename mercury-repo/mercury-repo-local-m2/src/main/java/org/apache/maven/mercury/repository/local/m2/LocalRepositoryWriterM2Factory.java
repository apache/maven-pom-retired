package org.apache.maven.mercury.repository.local.m2;

import org.apache.maven.mercury.repository.api.AbstractRepository;
import org.apache.maven.mercury.repository.api.LocalRepository;
import org.apache.maven.mercury.repository.api.Repository;
import org.apache.maven.mercury.repository.api.RepositoryException;
import org.apache.maven.mercury.repository.api.RepositoryWriter;
import org.apache.maven.mercury.repository.api.RepositoryWriterFactory;
import org.codehaus.plexus.i18n.DefaultLanguage;
import org.codehaus.plexus.i18n.Language;

public class LocalRepositoryWriterM2Factory
implements RepositoryWriterFactory
{
  private static final Language lang = new DefaultLanguage( LocalRepositoryWriterM2Factory.class );
  private static final LocalRepositoryWriterM2Factory factory = new LocalRepositoryWriterM2Factory();
  
  static 
  {
    AbstractRepository.register( AbstractRepository.DEFAULT_REPOSITORY_TYPE, factory  );
  }
  
  public RepositoryWriter getWriter( Repository repo )
  throws RepositoryException
  {
    if( repo == null || !(repo instanceof LocalRepository) )
      throw new RepositoryException( lang.getMessage( "bad.repository.type", repo == null ? "null" : repo.getClass().getName() ) );
    
    return new LocalRepositoryWriterM2( (LocalRepository)repo );
  }

}
