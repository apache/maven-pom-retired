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

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class AttributeQuery
{
  public static final char EXRP_START = '{';
  public static final char EXRP_STOP  = '}';
  
  private String expr;
  
  public AttributeQuery( String query )
  {
    if( query == null || query.indexOf( EXRP_START ) == -1 )
      return;
    int from = query.indexOf( EXRP_START );
    int to   = query.indexOf( EXRP_STOP );
  }
  
  public static String stripExpression( String query )
  {
    if( query == null || query.indexOf( EXRP_START ) == -1 )
      return query;
    
    int from = query.indexOf( EXRP_START );
    if( from == 0 )
      return null;

    return query.substring( 0, from );
  }
}
