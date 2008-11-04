package org.apache.maven.mercury.metadata;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.maven.mercury.artifact.api.ArtifactListProcessor;
import org.apache.maven.mercury.repository.api.Repository;
import org.apache.maven.mercury.repository.api.RepositoryException;
import org.codehaus.plexus.lang.DefaultLanguage;
import org.codehaus.plexus.lang.Language;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class DependencyBuilderFactory
{
  public static final String JAVA_DEPENDENCY_MODEL = "java";
  public static final String OSGI_DEPENDENCY_MODEL = "osgi";

  private static final Language _lang = new DefaultLanguage( DependencyBuilderFactory.class) ;
  
  public static final DependencyBuilder create(
        final String dependencyModel
      , final Collection<Repository> repositories
      , final Collection<MetadataTreeArtifactFilter> filters
      , final List<Comparator<MetadataTreeNode>> comparators
      , final Map<String,ArtifactListProcessor> processors
                     )
  throws RepositoryException
  {
    if( JAVA_DEPENDENCY_MODEL.equals( dependencyModel ) )
      return new DependencyTreeBuilder( repositories,  filters, comparators, processors );
    
    throw new IllegalArgumentException( _lang.getMessage( "dependency.model.not.implemented", dependencyModel ) );
  }

}
