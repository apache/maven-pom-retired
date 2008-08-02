package org.apache.maven.mercury.repository.api;

import org.apache.maven.mercury.builder.api.MetadataProcessor;

public interface RepositoryWriterFactory
{
  public RepositoryWriter getWriter( Repository repo, MetadataProcessor mdProcessor )
  throws RepositoryException;
}
