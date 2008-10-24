package org.apache.maven.mercury.repository.tests;

import java.util.Arrays;
import java.util.List;

import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class THelper
{
  public static List<ArtifactBasicMetadata> toList( ArtifactBasicMetadata... bmds)
  {
    return Arrays.asList( bmds );
  }
}
