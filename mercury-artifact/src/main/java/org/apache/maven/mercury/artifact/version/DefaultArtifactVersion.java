package org.apache.maven.mercury.artifact.version;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

import java.util.StringTokenizer;

import org.apache.maven.mercury.artifact.Quality;


/*
 * Default implementation of artifact versioning.
 * 
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * 
 * @version $Id: DefaultArtifactVersion.java 660026 2008-05-25 20:21:53Z jvanzyl $
 */
public class DefaultArtifactVersion
    implements ArtifactVersion
{
  private String version;
  
  private Integer majorVersion;

  private Integer minorVersion;

  private Integer incrementalVersion;

  private Integer buildNumber;

  private String base;

  private String qualifier;

  private ComparableVersion comparable;
  
  private Quality quality = Quality.UNKNOWN_QUALITY;

  public DefaultArtifactVersion( String version )
  {
    this.version = version;
    parseVersion( version );
    parseQuality( version );
  }
  
  public String getBase()
  {
    return base;
  }
  
  public boolean sameBase( DefaultArtifactVersion v )
  {
    return base.equals( v.base );
  }
  
  public boolean sameBase( String vs )
  {
    DefaultArtifactVersion v = new DefaultArtifactVersion(vs);
    return base.equals( v.base );
  }

  @Override
  public int hashCode()
  {
    return 11 + comparable.hashCode();
  }

  @Override
  public boolean equals( Object other )
  {
      return compareTo( other ) == 0;
  }

  public int compareTo( Object o )
  {
      DefaultArtifactVersion otherVersion = (DefaultArtifactVersion) o;
      return this.comparable.compareTo( otherVersion.comparable );
  }

  public int getMajorVersion()
  {
      return majorVersion != null ? majorVersion : 0;
  }

  public int getMinorVersion()
  {
      return minorVersion != null ? minorVersion : 0;
  }

  public int getIncrementalVersion()
  {
      return incrementalVersion != null ? incrementalVersion : 0;
  }

  public int getBuildNumber()
  {
      return buildNumber != null ? buildNumber : 0;
  }

  public String getQualifier()
  {
      return qualifier;
  }

  public final void parseVersion( String version )
  {
      comparable = new ComparableVersion( version );

      int index = version.indexOf( "-" );

      String part1;
      String part2 = null;

      if ( index < 0 )
      {
          part1 = version;
      }
      else
      {
          part1 = version.substring( 0, index );
          part2 = version.substring( index + 1 );
      }

      if ( part2 != null )
      {
          try
          {
              if ( ( part2.length() == 1 ) || !part2.startsWith( "0" ) )
              {
                  buildNumber = Integer.valueOf( part2 );
              }
              else
              {
                  qualifier = part2;
              }
          }
          catch ( NumberFormatException e )
          {
              qualifier = part2;
          }
      }

      if ( ( part1.indexOf( "." ) < 0 ) && !part1.startsWith( "0" ) )
      {
          try
          {
              majorVersion = Integer.valueOf( part1 );
          }
          catch ( NumberFormatException e )
          {
              // qualifier is the whole version, including "-"
              qualifier = version;
              buildNumber = null;
          }
      }
      else
      {
          boolean fallback = false;
          StringTokenizer tok = new StringTokenizer( part1, "." );
          try
          {
              majorVersion = getNextIntegerToken( tok );
              if ( tok.hasMoreTokens() )
              {
                  minorVersion = getNextIntegerToken( tok );
              }
              if ( tok.hasMoreTokens() )
              {
                  incrementalVersion = getNextIntegerToken( tok );
              }
              if ( tok.hasMoreTokens() )
              {
                  fallback = true;
              }
          }
          catch ( NumberFormatException e )
          {
              fallback = true;
          }

          if ( fallback )
          {
              // qualifier is the whole version, including "-"
              qualifier = version;
              base = "";
              majorVersion = null;
              minorVersion = null;
              incrementalVersion = null;
              buildNumber = null;
          }
      }

      if( base == null )
      {
        if( qualifier == null )
          base = version;
        else
        {
          int ind = version.indexOf( qualifier );
          if( ind == 0 )
            base = qualifier;
          else
            base = version.substring( 0, version.indexOf( qualifier )-1 );
        }
      }
  }

  private static Integer getNextIntegerToken( StringTokenizer tok )
  {
      String s = tok.nextToken();
      if ( ( s.length() > 1 ) && s.startsWith( "0" ) )
      {
          throw new NumberFormatException( "Number part has a leading 0: '" + s + "'" );
      }
      return Integer.valueOf( s );
  }
  
  private void parseQuality( String version )
  {
    quality = new Quality( version );
  }

  @Override
  public String toString()
  {
      StringBuffer buf = new StringBuffer();
      if ( majorVersion != null )
      {
          buf.append( majorVersion );
      }
      if ( minorVersion != null )
      {
          buf.append( "." );
          buf.append( minorVersion );
      }
      if ( incrementalVersion != null )
      {
          buf.append( "." );
          buf.append( incrementalVersion );
      }
      if ( buildNumber != null )
      {
          buf.append( "-" );
          buf.append( buildNumber );
      }
      else if ( qualifier != null )
      {
          if ( buf.length() > 0 )
          {
              buf.append( "-" );
          }
          buf.append( qualifier );
      }
      return buf.toString();
  }
  
  public Quality getQuality()
  {
    return quality;
  }
}
