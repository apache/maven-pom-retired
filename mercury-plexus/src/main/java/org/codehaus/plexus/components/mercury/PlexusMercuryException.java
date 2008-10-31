package org.codehaus.plexus.components.mercury;

public class PlexusMercuryException
    extends Exception
{

  public PlexusMercuryException()
  {
  }

  public PlexusMercuryException(
      String message )
  {
    super( message );
  }

  public PlexusMercuryException(
      Throwable cause )
  {
    super( cause );
  }

  public PlexusMercuryException(
      String message,
      Throwable cause )
  {
    super( message, cause );
  }

}
