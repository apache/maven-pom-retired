package org.apache.maven.mercury.repository.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.mercury.ArtifactBasicMetadata;
/**
 * generic repository operation result. Represents a List of <T extends ArtifactBasicMetadata> 
 * objects and a set of exceptions thrown in the process 
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 * @param <T>
 */
public class RepositoryOperationResult<T extends ArtifactBasicMetadata>
{
  private Set<RepositoryException> _exceptions = Collections.synchronizedSet( new HashSet<RepositoryException>() );
  private List<T> _results = Collections.synchronizedList( new ArrayList<T>() );

  public RepositoryOperationResult()
  {
  }

  protected void add( RepositoryException exception )
  {
      _exceptions.add( exception );
  }

  public Set<RepositoryException> getExceptions()
  {
      return _exceptions;
  }

  public String toString()
  {
      return _exceptions.toString();
  }
  
  public boolean hasExceptions()
  {
      return _exceptions.size() > 0;
  }
  
  public void add( T result )
  {
    _results.add( result );
  }
  
  public void add( Collection<T> resColl )
  {
    _results.addAll( resColl );
  }
  
  public List<T> getResults()
  {
    return _results;
  }
  
  public boolean hasResults()
  {
      return _results.size() > 0;
  }
  
}
