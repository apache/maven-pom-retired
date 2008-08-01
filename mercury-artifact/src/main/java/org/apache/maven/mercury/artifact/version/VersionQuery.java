package org.apache.maven.mercury.artifact.version;

import java.util.StringTokenizer;

/**
 * multiple ranges. Not sure if we need need it - will delete later ..
 * 
 * @author Oleg Gusakov
 * @version $Id$
 */
public class VersionQuery
{
  String [] _ranges;
  
  public VersionQuery( String query )
  {
    
  }
  
  private void parseRange( String query )
  {
    if( query == null || query.length() < 1 )
      return;
    
    StringTokenizer st = new StringTokenizer( query, "," );
    int nRanges = st.countTokens();
    if( nRanges < 1 )
      return;
    
    _ranges = new String [ nRanges ];
    int count = 0;
    
    while( st.hasMoreTokens() )
      _ranges[ count ++ ] = st.nextToken();
    
  }

  /**
   * 
   */
  public boolean within( String version )
  {
    return false;
  }
}
