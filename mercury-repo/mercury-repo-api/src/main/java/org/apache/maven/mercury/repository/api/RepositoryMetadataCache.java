package org.apache.maven.mercury.repository.api;

import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;

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
   * check if GA level metadata exists in this cache for the given repo. Read from repo, if does not exists
   * 
   * @param bmd - bare GA coordinates of the requisted metadata
   * @return 
   */
  public RepositoryGAMetadata findGA( RemoteRepository repo, ArtifactBasicMetadata bmd );
  
  /**
   * check if GAV level metadata exists in this cache for the given repo. Read from repo, if does not exists
   * 
   * @param bmd - bare GAV coordinates of the requisted metadata
   * @return 
   */
  public RepositoryGAMetadata findGAV( RemoteRepository repo, ArtifactBasicMetadata bmd );
}
