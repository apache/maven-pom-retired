/**
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
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
