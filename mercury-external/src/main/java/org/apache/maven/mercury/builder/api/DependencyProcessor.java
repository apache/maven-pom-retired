package org.apache.maven.mercury.builder.api;

import java.util.Hashtable;
import java.util.List;

import org.apache.maven.mercury.artifact.ArtifactBasicMetadata;

public interface DependencyProcessor
{
  
  public static final DependencyProcessor NULL_PROCESSOR = 
                        new DependencyProcessor() {
                          public List<ArtifactBasicMetadata> getDependencies( ArtifactBasicMetadata bmd, MetadataReader mdReader, Hashtable env )
                          throws MetadataReaderException
                          {
                            return null;
                          }
                        };

  List<ArtifactBasicMetadata> getDependencies( ArtifactBasicMetadata bmd, MetadataReader mdReader, Hashtable env )
  throws MetadataReaderException;
}
