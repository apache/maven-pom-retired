package org.apache.maven.mercury.artifact;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public enum QualityEnum
{
  snapshot(1)
  , alpha(2)
  , beta(3)
  , release(10)
  ;

  public static final QualityEnum DEFAULT_QUALITY = snapshot;

  private int id;

  // Constructor
  QualityEnum( int id )
  {
      this.id = id;
  }

  int getId()
  {
      return id;
  }
  
}
