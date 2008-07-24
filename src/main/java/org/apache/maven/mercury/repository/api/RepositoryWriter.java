package org.apache.maven.mercury.repository.api;

import org.apache.maven.mercury.Artifact;


/**
 * Repository wruter API to be implemented by any repo implementation that wishes 
 * to store artifacts for Maven. All operations are asynchronous and can generate
 * callback events
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public interface RepositoryWriter
{
  /**
   * write (upload) given artifact to the repository
   * 
   * @param artifact to upload
   * @throws RepositoryException
   */
  public void writeArtifact( Artifact artifact, RepositoryCallback callback )
  throws RepositoryException;
}
