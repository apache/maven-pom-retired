package org.apache.maven.mercury.repository.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.maven.mercury.artifact.Quality;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class RepositoryManager
{
  protected static transient List<Repository> _repoList = Collections.synchronizedList( new ArrayList<Repository>(8) );
  
  void setRepositories()
  {
    
  }
  
  List<Repository> getRepositories()
  {
    return _repoList;
  }
  
  LocalRepository findLocal( Quality aq )
  {
    for( Repository r : _repoList )
    {
      if( r.isLocal() && !r.isReadOnly() && r.isAcceptedQuality( aq ) )
        return (LocalRepository)r;
    }
    return null;
  }
}
