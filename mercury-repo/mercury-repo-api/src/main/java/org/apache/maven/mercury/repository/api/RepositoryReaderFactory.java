package org.apache.maven.mercury.repository.api;

public interface RepositoryReaderFactory
{
  public RepositoryReader getReader( Repository repo, MetadataProcessor mdProcessor )
  throws RepositoryException;
}
