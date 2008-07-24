package org.apache.maven.mercury.repository.api;

import java.util.List;

/**
 * This is a generic repository event callback. Used by all repository operations
 * to signal end of operation and represents a list of exceptions plus a list
 * of ArtifactBasicMetadata derivatives
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public interface RepositoryCallback
{
  public void done( RepositoryOperationResult<?> result );
}
