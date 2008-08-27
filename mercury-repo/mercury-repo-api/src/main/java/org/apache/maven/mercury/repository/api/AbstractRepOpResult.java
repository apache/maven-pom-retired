package org.apache.maven.mercury.repository.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;
/**
 * generic repository operation result. Represents a Map of query object to AbstractRepositoryOperationResult
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public abstract class AbstractRepOpResult
{
  private Map<ArtifactBasicMetadata,Exception> _exceptions;
  
  public AbstractRepOpResult()
  {
  }
  
  public Map<ArtifactBasicMetadata,Exception> getExceptions()
  {
      return _exceptions;
  }
  
  public abstract boolean hasResults();
  
  public abstract boolean hasResults( ArtifactBasicMetadata key );
  
  public boolean hasExceptions()
  {
    return _exceptions != null && ! _exceptions.isEmpty();
  }
  
  public void addError( ArtifactBasicMetadata key, Exception error )
  {
    if( _exceptions == null )
      _exceptions = new HashMap<ArtifactBasicMetadata, Exception>(8);

    _exceptions.put( key, error );
  }
  
  public Exception getError( ArtifactBasicMetadata key )
  {
    if( _exceptions == null )
      return null;

    return _exceptions.get( key );
  }

  public String toString()
  {
      return _exceptions.toString();
  }
}
