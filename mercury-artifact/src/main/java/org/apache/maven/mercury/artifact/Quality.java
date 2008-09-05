package org.apache.maven.mercury.artifact;


/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class Quality
implements Comparable<Quality>
{
  public static final int DEFAULT_QUANTITY = 0;
  public static final int SNAPSHOT_TS_QUANTITY = 1;

  public static final int FIXED_RELEASE_QUANTITY = -2;
  public static final int FIXED_LATEST_QUANTITY  = -3;

  public static final Quality UNKNOWN_QUALITY = new Quality( QualityEnum.unknown, -1 );
  public static final Quality SNAPSHOT_QUALITY = new Quality( QualityEnum.snapshot, DEFAULT_QUANTITY );
  public static final Quality SNAPSHOT_TS_QUALITY = new Quality( QualityEnum.snapshot, SNAPSHOT_TS_QUANTITY );
  public static final Quality ALPHA_QUALITY = new Quality( QualityEnum.alpha, DEFAULT_QUANTITY );
  public static final Quality BETA_QUALITY = new Quality( QualityEnum.beta, DEFAULT_QUANTITY );
  public static final Quality RELEASE_QUALITY = new Quality( QualityEnum.release, DEFAULT_QUANTITY );

  public static final Quality FIXED_RELEASE_QUALITY = new Quality( QualityEnum.unknown, FIXED_RELEASE_QUANTITY );
  public static final Quality FIXED_LATEST_QUALITY = new Quality( QualityEnum.unknown, FIXED_LATEST_QUANTITY );
  
  private static final String snExp    = ".+-(SNAPSHOT|\\d{8}\\.\\d{6}-\\d+)";
  private static final String alphaExp = ".+-alpha-\\d+";
  private static final String betaExp  = ".+-beta-\\d+";

  protected QualityEnum quality;
  protected int quantity;
  
  public Quality( QualityEnum quality, int quantity )
  {
    this.quality = quality;;
    this.quantity = quantity;
  }
  
  public Quality( String version )
  {
    if( version == null || version.length() < 1 )
    {
      quality = QualityEnum.unknown;
      quantity = -1;
      return;
    }
    
    if( Artifact.RELEASE_VERSION.equals( version  ) )
    {
      quality = QualityEnum.unknown;
      quantity = FIXED_RELEASE_QUANTITY;
      return;
    }
    
    if( Artifact.LATEST_VERSION.equals( version  ) )
    {
      quality = QualityEnum.unknown;
      quantity = FIXED_LATEST_QUANTITY;
      return;
    }
    
    if( version.matches( snExp ) )
    {
      quality = QualityEnum.snapshot;
      if( version.endsWith( Artifact.SNAPSHOT_VERSION ) )
        quantity = DEFAULT_QUANTITY;
      else
        quantity = SNAPSHOT_TS_QUANTITY;
      return;
    }
    
    if( version.matches( alphaExp ) )
    {
      quality = QualityEnum.alpha;
      quantity = Integer.parseInt( version.substring( version.lastIndexOf( '-' )+1 ) );
      return;
    }
    
    if( version.matches( betaExp ) )
    {
      quality = QualityEnum.beta;
      quantity = Integer.parseInt( version.substring( version.lastIndexOf( '-' )+1 ) );
      return;
    }
    
    quality = QualityEnum.release;
    quantity = DEFAULT_QUANTITY;
    
  }
  
  public int compareTo( Quality q )
  {
    if( q == null )
      return quality == null ? 0 : 1;
    
    int ql = (quality == null ? QualityEnum.unknown : quality).getId();
    int ql2 = (q.quality == null ? QualityEnum.unknown : q.quality).getId();
    
    if( ql == ql2 )
    {
      // snapshots are always equal
      if( ql == QualityEnum.snapshot.getId() )
        return 0;
      else
        return quantity - q.quantity;
    }
    
    // unknown is less'n anyone
//    if( ql1 == QualityEnum.unknown.getId() )
//      return -1;

    return sign(ql - ql2);
  }
  
  private static int sign( int i )
  {
    return i<0 ? -1:1;
  }
  
  @Override
  public boolean equals( Object obj )
  {
    if( obj == null )
    {
      return false;
    }
    
    if( obj.getClass().isAssignableFrom( Quality.class ))
      return this.compareTo( (Quality)obj ) == 0;
    else if( obj.getClass().isAssignableFrom( QualityEnum.class ))
            return this.compareTo( (QualityEnum)obj ) == 0;
    
    return super.equals( obj );
  }

  public int compareTo( QualityEnum qe )
  {
    if( qe == null )
      return quality == null ? 0 : 1;
    
    int ql1 = (quality == null ? QualityEnum.unknown : quality).getId();
    
    return sign(ql1 - qe.getId());
  }

  public QualityEnum getQuality()
  {
    return quality;
  }

  public int getQuantity()
  {
    return quantity;
  }

}
