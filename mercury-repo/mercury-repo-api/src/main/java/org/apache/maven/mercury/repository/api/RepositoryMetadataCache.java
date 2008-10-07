package org.apache.maven.mercury.repository.api;

import java.util.Collection;

import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;
import org.apache.maven.mercury.repository.metadata.Metadata;

/**
 * this object abstracts the existence of multiple repositories and repository
 * policies. Given a metadata request, implementor of this interface will
 * either return a local copy if one exists, or will go out and read from a remote repo
 * if either local copy does not exist or remote repository policy allows a read 
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public interface RepositoryMetadataCache
{
  /**
   * initialize cache implementor with a collection of remote repositories. Order 
   * does not matter here because access is defined by repository policy.
   * 
   * @param repos
   */
  public void init( Collection<RemoteRepository> repos );
  
  /**
   * 
   * @param bmd
   * @return 
   */
  public Metadata findGA( ArtifactBasicMetadata bmd );
}
