package org.apache.maven.mercury.repository.api;

import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;

/**
 * This interface implementation is supplied to MetadataProcessor to simplify it's access to remote repositories
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public interface MetadataReader
{
  /**
   * read content pointed by bmd. It will return POM bytes regardless of actual bmd type
   * 
   * @param bmd coordinates
   * @param classifier - replaces the getClassifier() from bmd if not null
   * @param type - replaces the getType() from bmd if not null
   * @return
   * @throws MetadataProcessingException
   */
  public byte [] readRawData( ArtifactBasicMetadata bmd, String classifier, String type )
  throws MetadataProcessingException;

  /**
   * read metadata for the artifact, pointed by bmd. It will return POM bytes regardless of actual bmd type
   * 
   * @param bmd
   * @return
   * @throws MetadataProcessingException
   */
  public byte [] readMetadata( ArtifactBasicMetadata bmd )
  throws MetadataProcessingException;
}
