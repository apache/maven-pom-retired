package org.apache.maven.mercury.repository.api;

import java.util.List;
import java.util.Map;

import org.apache.maven.mercury.Artifact;
import org.apache.maven.mercury.ArtifactBasicMetadata;
import org.apache.maven.mercury.ArtifactMetadata;
import org.apache.maven.mercury.DefaultArtifact;
import org.apache.maven.mercury.repository.Repository;

/**
 * Repository reader API to be implemented by any repo implementation that wishes 
 * to serve artifacts to the build process
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public interface RepositoryReader
{
  /**
   * given basic coordinates query - instantiate all available matches as ArtifactBasicMetadata objects. 
   * <b>Analogous to reading maven-metadata.xml</b> file from GA folder i.e. this transforms
   * GA[Vrange] -> [GAV1, GAV2, ... GAVn]
   * 
   * @param query list of MD coordinate queries to find 
   * @return map of results - lists of available matches.  
   * <b>If no results are found, reader should return null<b> If there were exceptions, map element will indicate 
   * it with hasExceptions() 
   * @throws RepositoryException
   */
  public Map<ArtifactBasicMetadata, RepositoryOperationResult<ArtifactBasicMetadata>> readVersions( List<? extends ArtifactBasicMetadata> query )
  throws RepositoryException, IllegalArgumentException;
  
  /**
   * given basic coordinates query read full ArtifactMetadata objects -
   * with dependencies as queries i.e. each dependency at this stage is an ArtifactBasicMetadata
   * <b>Analogous to reading pom.xml</b> file for given GAV
   * 
   * @param query list of MD coordinate queries to read. They are found by previous call to findMetadata 
   * @return result as list of available MD objects with dependencies filled in. Order is the same 
   * as in query list. null means not found or worse
   * @throws RepositoryException
   */
  public RepositoryOperationResult<ArtifactMetadata> readDependencies( List<? extends ArtifactBasicMetadata> query )
  throws RepositoryException, IllegalArgumentException;

  /**
   * Given basic coordinates query read Artifact objects
   * Analogous to downloading artifact binary  file into local repo for given GAV
   * 
   * @param query list of MD coordinate queries to read. 
   * @return array of results - lists of available matches. Order is the same as in query list. null means not found or worse
   * @throws RepositoryException
   */
  public RepositoryOperationResult<DefaultArtifact> readArtifacts( List<? extends ArtifactBasicMetadata> query )
  throws RepositoryException, IllegalArgumentException;

  /**
   * Need if for explanation function - where and how(protocol) this artifact is found.
   */
  public Repository getRepository();
}
