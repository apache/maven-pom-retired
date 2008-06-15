package org.apache.maven.mercury.metadata.builder;

import java.util.List;
import java.util.Properties;

import org.apache.maven.mercury.metadata.ArtifactMetadata;

public class XmlMetadataBuilder
implements MetadataBuilder
{
  
  public XmlMetadataBuilder( Properties props )
  {
  }

  public List<ArtifactMetadata> find(ArtifactMetadata query)
      throws MetadataBuilderException
  {
    return null;
  }

}
