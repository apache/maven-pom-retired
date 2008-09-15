package org.apache.maven.mercury.repository.api;

import org.apache.maven.mercury.builder.api.DependencyProcessor;

public interface RepositoryReaderFactory
{
  public RepositoryReader getReader( Repository repo, DependencyProcessor mdProcessor )
  throws RepositoryException;
}
