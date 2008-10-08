package org.apache.maven.mercury.repository.api;


/**
 * helper class for writing repository writers
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class AbstractRepositoryWriter
extends Thread
{
  
  protected RepositoryMetadataCache _mdCache;
  
  public void setMetadataCache( RepositoryMetadataCache mdCache )
  {
    this._mdCache = mdCache;
  }
  
  public RepositoryMetadataCache getMetadataCache()
  {
    return _mdCache;
  }
  
  public boolean hasMetadataCache()
  {
    return _mdCache != null;
  }
}
