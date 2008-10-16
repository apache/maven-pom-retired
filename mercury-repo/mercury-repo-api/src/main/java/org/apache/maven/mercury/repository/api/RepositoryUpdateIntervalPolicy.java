package org.apache.maven.mercury.repository.api;

import java.text.ParseException;

import org.apache.maven.mercury.util.TimeUtil;
import org.apache.maven.mercury.util.Util;
import org.codehaus.plexus.lang.DefaultLanguage;
import org.codehaus.plexus.lang.Language;

/**
 * implements current maven update policy
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class RepositoryUpdateIntervalPolicy
implements RepositoryUpdatePolicy
{
  private static final Language _lang = new DefaultLanguage( RepositoryUpdateIntervalPolicy.class );
  
  public static final String UPDATE_POLICY_NEVER = "never";

  public static final String UPDATE_POLICY_ALWAYS = "always";

  public static final String UPDATE_POLICY_DAILY = "daily";

  public static final String UPDATE_POLICY_INTERVAL = "interval";
  private static final int UPDATE_POLICY_INTERVAL_LENGTH = UPDATE_POLICY_INTERVAL.length();
  
  public static final String DEFAULT_UPDATE_POLICY = UPDATE_POLICY_DAILY;

  private static final long NEVER = -1L;
  
  private static final long DAYLY = 3600000L*24L;
  
  protected long interval = DAYLY;
  
  
  public RepositoryUpdateIntervalPolicy()
  {
  }

  public RepositoryUpdateIntervalPolicy( String policy )
  {
    init( policy );
  }
  
  /**
   * used mostly for testing as it's too much waiting for a minute to test expiration
   * 
   * @param interval
   */
  public RepositoryUpdateIntervalPolicy setInterval( long interval )
  {
    this.interval = interval;
    return this;
  }

  public void init( String policy )
  {
     if( Util.isEmpty( policy ) )
       throw new IllegalArgumentException( _lang.getMessage( "empty.policy", policy ));
     
     if( policy.startsWith( UPDATE_POLICY_ALWAYS ) )
       interval = 0L;
     else if( policy.startsWith( UPDATE_POLICY_DAILY ) )
       interval = DAYLY;
     else if( policy.startsWith( UPDATE_POLICY_NEVER ) )
       interval = NEVER;
     else if( policy.startsWith( UPDATE_POLICY_INTERVAL ) )
     {
       int len = policy.length();
       if( len <= UPDATE_POLICY_INTERVAL_LENGTH )
         throw new IllegalArgumentException( _lang.getMessage( "bad.interval.policy", policy ));

       interval = Integer.parseInt( policy.substring( len-1 ) ) * 60000L;
     }
     else
       throw new IllegalArgumentException( _lang.getMessage( "bad.policy", policy ));
  }

  public boolean timestampExpired( long lastUpdateMillis )
  {
    if( interval == NEVER )
      return false;
    
    long now;
    try
    {
      now = TimeUtil.toMillis( TimeUtil.getUTCTimestamp() );
    }
    catch( ParseException e )
    {
      throw new IllegalArgumentException( e );
    }
    
    boolean res = ( (now - lastUpdateMillis) > interval); 
    
    return res;
  }
  
  public static void main(
      String[] args )
  {
    RepositoryUpdateIntervalPolicy up = new RepositoryUpdateIntervalPolicy("interval2");
    
    System.out.println("Interval is "+up.interval);
  }

}
