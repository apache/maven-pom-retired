package org.apache.maven.mercury.compare;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public interface IDepResolver
{
  void resolve( String groupI, String artifactId, String version, String type )
  throws Exception;
  
}
