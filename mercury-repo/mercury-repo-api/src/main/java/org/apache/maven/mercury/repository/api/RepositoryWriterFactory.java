package org.apache.maven.mercury.repository.api;

public interface RepositoryWriterFactory
{
  public RepositoryWriter getWriter( Repository repo, MetadataProcessor mdProcessor )
  throws RepositoryException;
}
