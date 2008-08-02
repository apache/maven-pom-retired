package org.apache.maven.mercury.repository.api;

import org.apache.maven.mercury.builder.api.MetadataProcessor;

public interface RepositoryReaderFactory
{
  public RepositoryReader getReader( Repository repo, MetadataProcessor mdProcessor )
  throws RepositoryException;
}
