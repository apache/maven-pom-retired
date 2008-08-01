package org.apache.maven.mercury.repository.api;

/**
 * parent of all repository accessors - readers and writers
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public interface RepositoryOperator
{
  public String [] getProtocols();
  public boolean canHandle( String protocol );
  public void close();
}
