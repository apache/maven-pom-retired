package org.apache.maven.mercury.util;

import java.util.Date;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class TimeUtil
{

  /**
   * 
   * @return current UTC timestamp by yyyyMMddHHmmss mask
   */
  public static String getUTCTimestamp( )
  {
    return getUTCTimestamp( new Date() );
  }

  /**
   * 
   * @return current UTC timestamp by yyyyMMddHHmmss mask as a long int
   */
  public static long getUTCTimestampAsLong( )
  {
    return Long.parseLong( getUTCTimestamp( new Date() ) );
  }

  /**
   * 
   * @param date
   * @return current date converted to UTC timestamp by yyyyMMddHHmmss mask
   */
  public static String getUTCTimestamp( Date date )
  {
      java.util.TimeZone timezone = java.util.TimeZone.getTimeZone( "UTC" );
      java.text.DateFormat fmt = new java.text.SimpleDateFormat( "yyyyMMddHHmmss" );
      fmt.setTimeZone( timezone );
      return fmt.format( date );
  }

}
