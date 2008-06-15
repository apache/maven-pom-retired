package org.apache.maven.mercury.metadata.builder;

public class MetadataBuilderException
    extends Exception
{

  public MetadataBuilderException()
  {
  }

  public MetadataBuilderException(String message)
  {
    super(message);
  }

  public MetadataBuilderException(Throwable cause)
  {
    super(cause);
  }

  public MetadataBuilderException(String message, Throwable cause)
  {
    super(message, cause);
  }

}
