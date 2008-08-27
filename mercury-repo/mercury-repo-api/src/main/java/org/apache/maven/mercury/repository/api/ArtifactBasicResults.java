package org.apache.maven.mercury.repository.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;

/**
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class ArtifactBasicResults
extends AbstractRepOpResult
{
  Map< ArtifactBasicMetadata, List<ArtifactBasicMetadata>> _result = new HashMap<ArtifactBasicMetadata, List<ArtifactBasicMetadata>>(8);

  /**
   * first result is ready 
   */
  public ArtifactBasicResults( ArtifactBasicMetadata query, List<ArtifactBasicMetadata> result )
  {
    this._result.put( query, result );
  }
  
  /**
   * optimization opportunity
   * 
   * @param size
   */
  public ArtifactBasicResults( int size )
  {
  }

  private ArtifactBasicResults()
  {
  }

  public static ArtifactBasicResults add( final ArtifactBasicResults res, final ArtifactBasicMetadata key, final Exception err )
  {
    ArtifactBasicResults ret = res;
    if( res == null )
      ret = new ArtifactBasicResults();
    
    ret.addError( key, err );
    
    return ret;
  }

  public static ArtifactBasicResults add( final ArtifactBasicResults res, final ArtifactBasicMetadata key, final List<ArtifactBasicMetadata> result )
  {
    ArtifactBasicResults ret = res;
    if( res == null )
      ret = new ArtifactBasicResults();
    
    ret.add( key, result );
    
    return ret;
  }

  public static ArtifactBasicResults add( final ArtifactBasicResults res, final ArtifactBasicMetadata key, final ArtifactBasicMetadata result )
  {
    ArtifactBasicResults ret = res;
    if( res == null )
      ret = new ArtifactBasicResults();
    
    ret.add( key, result );
    
    return ret;
  }
  
  private List<ArtifactBasicMetadata> getOrCreate( ArtifactBasicMetadata query )
  {
    List<ArtifactBasicMetadata> res = _result.get( query );
    if( res == null )
    {
      res = new ArrayList<ArtifactBasicMetadata>(8);
      _result.put( query, res );
    }
    return res;
  }
  
  public void add( ArtifactBasicMetadata query, List<ArtifactBasicMetadata> result )
  {
    List<ArtifactBasicMetadata> res = getOrCreate( query );
    res.addAll( result );
  }
  
  public void add( ArtifactBasicMetadata query, ArtifactBasicMetadata result )
  {
    List<ArtifactBasicMetadata> res = getOrCreate( query );
    res.add( result );
  }

  public Map< ArtifactBasicMetadata, List<ArtifactBasicMetadata>> getResults()
  {
    return _result;
  }

  public List<ArtifactBasicMetadata> getResult( ArtifactBasicMetadata query )
  {
    return _result.get( query );
  }

  @Override
  public boolean hasResults()
  {
    return ! _result.isEmpty();
  }

  @Override
  public boolean hasResults( ArtifactBasicMetadata key )
  {
    return ! _result.isEmpty() && _result.containsKey( key ) && ! _result.get( key ).isEmpty();
  }

}
