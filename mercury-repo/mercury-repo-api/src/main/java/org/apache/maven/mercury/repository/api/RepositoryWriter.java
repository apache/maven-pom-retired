package org.apache.maven.mercury.repository.api;

import java.util.Collection;

import org.apache.maven.mercury.artifact.Artifact;


/**
 * Repository writer API to be implemented by any repo implementation that wishes 
 * to store artifacts for Maven. All operations are asynchronous and can generate
 * callback events
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public interface RepositoryWriter
extends RepositoryOperator
{
  /**
   * write (upload) given artifact to the repository
   * 
   * @param artifact to upload
   * @throws RepositoryException
   */
  public void writeArtifact( Collection<Artifact> artifact )
  throws RepositoryException;
}
