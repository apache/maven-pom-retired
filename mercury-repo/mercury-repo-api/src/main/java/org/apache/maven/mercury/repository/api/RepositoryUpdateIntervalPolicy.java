package org.apache.maven.mercury.repository.api;

import org.apache.maven.mercury.artifact.version.VersionComparator;
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

  public boolean timeToUpdate( long lastUpdate )
  {
    long now =System.currentTimeMillis();
    
    boolean res = now - lastUpdate > interval; 
    
    return res;
  }

}
