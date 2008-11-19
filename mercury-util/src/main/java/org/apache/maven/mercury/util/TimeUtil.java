package org.apache.maven.mercury.util;

import java.text.ParseException;
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
  public static final java.util.TimeZone TS_TZ = java.util.TimeZone.getTimeZone( "UTC" );
  public static final java.text.DateFormat TS_FORMAT = new java.text.SimpleDateFormat( "yyyyMMddHHmmss" );
  
  static
  {
    TS_FORMAT.setTimeZone( TS_TZ );
  }

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
   * @return current UTC timestamp by yyyyMMddHHmmss mask as a long int
   */
  public static long getUTCTimestampAsMillis( )
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
      return TS_FORMAT.format( date );
  }

  /**
   * convert timestamp to millis
   * 
   * @param ts timestamp to convert. Presumed to be a long of form yyyyMMddHHmmss
   * @return millis, corresponding to the supplied TS
   * @throws ParseException is long does not follow the format
   */
  public static long toMillis( long ts )
  throws ParseException
  {
    return toMillis(  ""+ts );
  }

  /**
   * convert timestamp to millis
   * 
   * @param ts timestamp to convert. Presumed to be a string of form yyyyMMddHHmmss
   * @return millis, corresponding to the supplied TS
   * @throws ParseException is long does not follow the format
   */
  public static long toMillis( String ts )
  throws ParseException
  {
    Date dts =TS_FORMAT.parse( ts );
    
    return dts.getTime();
  }
}
