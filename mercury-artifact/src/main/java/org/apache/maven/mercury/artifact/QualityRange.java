package org.apache.maven.mercury.artifact;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class QualityRange
{
  public static final QualityRange SNAPSHOTS_ONLY = new QualityRange( Quality.SNAPSHOT_QUALITY, true, Quality.SNAPSHOT_TS_QUALITY, true );
  public static final QualityRange ALPHA_ONLY     = new QualityRange( Quality.SNAPSHOT_QUALITY, false, Quality.BETA_QUALITY, false );
  public static final QualityRange BETA_ONLY      = new QualityRange( Quality.ALPHA_QUALITY, false, Quality.RELEASE_QUALITY, false );
  public static final QualityRange RELEASES_ONLY  = new QualityRange( Quality.RELEASE_QUALITY, true, Quality.RELEASE_QUALITY, true );
  public static final QualityRange ALL            = new QualityRange( Quality.SNAPSHOT_QUALITY, true, Quality.RELEASE_QUALITY, true );
  
  protected Quality qualityFrom = Quality.SNAPSHOT_QUALITY;
  protected boolean fromInclusive = true;
  protected Quality qualityTo   = Quality.RELEASE_QUALITY;
  protected boolean toInclusive = true;

  /**
   * @param qualityFrom
   * @param fromInclusive
   * @param qualityTo
   * @param toInclusive
   */
  public QualityRange(
      Quality qualityFrom,
      boolean fromInclusive,
      Quality qualityTo,
      boolean toInclusive )
  {
    this.qualityFrom = qualityFrom;
    this.fromInclusive = fromInclusive;
    
    this.qualityTo = qualityTo;
    this.toInclusive = toInclusive;
  }
  
  //---------------------------------------------------------------------------
  public boolean isAcceptedQuality( Quality quality )
  {
    if( quality == null )
      return false;

    int from = quality.compareTo( qualityFrom );
    if( from == 0 )
      return fromInclusive;
    
    int to = quality.compareTo( qualityTo );
    if( to == 0 )
      return toInclusive;
    
    return from > 0 && to < 0;
  }

}
