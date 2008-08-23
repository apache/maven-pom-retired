package org.apache.maven.mercury.repository.api;

/**
 * 
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */

public interface RepositoryWriterFactory
{
  public RepositoryWriter getWriter( Repository repo )
  throws RepositoryException;
}
