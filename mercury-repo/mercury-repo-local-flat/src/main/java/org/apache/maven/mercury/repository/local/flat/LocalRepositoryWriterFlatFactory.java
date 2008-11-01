package org.apache.maven.mercury.repository.local.flat;

import org.apache.maven.mercury.repository.api.AbstractRepository;
import org.apache.maven.mercury.repository.api.LocalRepository;
import org.apache.maven.mercury.repository.api.Repository;
import org.apache.maven.mercury.repository.api.RepositoryException;
import org.apache.maven.mercury.repository.api.RepositoryWriter;
import org.apache.maven.mercury.repository.api.RepositoryWriterFactory;
import org.codehaus.plexus.lang.DefaultLanguage;
import org.codehaus.plexus.lang.Language;

public class LocalRepositoryWriterFlatFactory
implements RepositoryWriterFactory
{
  private static final Language lang = new DefaultLanguage( LocalRepositoryWriterFlatFactory.class );
  private static final LocalRepositoryWriterFlatFactory factory = new LocalRepositoryWriterFlatFactory();
  
  static 
  {
    AbstractRepository.register( LocalRepositoryFlat.FLAT_REPOSITORY_TYPE, factory  );
  }
  
  public RepositoryWriter getWriter( Repository repo )
  throws RepositoryException
  {
    if( repo == null || !(repo instanceof LocalRepository) )
      throw new RepositoryException( lang.getMessage( "bad.repository.type", repo == null ? "null" : repo.getClass().getName() ) );
    
    return new LocalRepositoryWriterFlat( (LocalRepository)repo );
  }

}
