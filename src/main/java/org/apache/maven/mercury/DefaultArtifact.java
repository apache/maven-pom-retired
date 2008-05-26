package org.apache.maven.mercury;

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

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;

import org.apache.maven.mercury.repository.Repository;
import org.codehaus.plexus.util.StringUtils;

/*
 * @author Jason van Zyl
 * 
 * @version $Id$
 */
public class DefaultArtifact
    implements Artifact
{
    private String groupId;

    private String artifactId;

    private String type;

    private File file;

    private String version;

    // This is Maven specific. jvz/
    private String classifier;

    private String scope;

    private boolean optional;

    private boolean release;

    private boolean resolved;

    // Why is this here? What repository is determined at runtime and is therefore a
    // runtime charactistic. This needs to go. jvz.
    private Repository repository;

    private String downloadUrl;

    private String inheritedScope;
    
    public DefaultArtifact( String groupId, String artifactId, String version, String type, String classifier, boolean optional, String scope, String inheritedScope )
    {
        if ( version == null )
        {
            throw new IllegalArgumentException( "Version cannot be null." );
        }

        initialize( groupId, artifactId, version, type, classifier, optional, scope, inheritedScope );
    }

    public String getInheritedScope()
    {
        return inheritedScope;
    }
    
    private void initialize( String groupId, String artifactId, String version, String type, String classifier, boolean optional, String scope, String inheritedScope )
    {
        this.inheritedScope = inheritedScope;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        //this.scope = scope;
        this.type = type;
        this.classifier = classifier;
        this.optional = optional;

        String desiredScope = Artifact.SCOPE_RUNTIME;

        boolean calc = true;

        if ( inheritedScope == null )
        {
            desiredScope = scope;
        }
        else if ( Artifact.SCOPE_TEST.equals( scope ) || Artifact.SCOPE_PROVIDED.equals( scope ) )
        {
            desiredScope = scope;
            //calc = false;
        }
        else if ( Artifact.SCOPE_COMPILE.equals( scope ) && Artifact.SCOPE_COMPILE.equals( inheritedScope ) )
        {
            // added to retain compile artifactScope. Remove if you want compile inherited as runtime
            desiredScope = Artifact.SCOPE_COMPILE;
        }

        if ( calc )
        {
            if ( Artifact.SCOPE_TEST.equals( inheritedScope ) )
            {
                desiredScope = Artifact.SCOPE_TEST;
            }

            if ( Artifact.SCOPE_PROVIDED.equals( inheritedScope ) )
            {
                desiredScope = Artifact.SCOPE_PROVIDED;
            }

            if ( Artifact.SCOPE_SYSTEM.equals( scope ) )
            {
                // system scopes come through unchanged...
                desiredScope = Artifact.SCOPE_SYSTEM;
            }
        }
        
        this.scope = desiredScope;

        validateIdentity();
    }

    private void validateIdentity()
    {
        if ( empty( groupId ) )
        {
            throw new IllegalArgumentException( "The groupId cannot be empty." );
        }

        if ( artifactId == null )
        {
            throw new IllegalArgumentException( "The artifactId cannot be empty." );
        }

        if ( type == null )
        {
            throw new IllegalArgumentException( "The type cannot be empty." );
        }

        if ( ( version == null ) )
        {
            throw new IllegalArgumentException( "The version cannot be empty." );
        }
    }

    private boolean empty( String value )
    {
        return ( value == null ) || ( value.trim().length() < 1 );
    }

    public String getClassifier()
    {
        return classifier;
    }

    public boolean hasClassifier()
    {
        return StringUtils.isNotEmpty( classifier );
    }

    public String getScope()
    {
        return scope;
    }

    public String getGroupId()
    {
        return groupId;
    }

    public String getArtifactId()
    {
        return artifactId;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion( String version )
    {
        this.version = version;
    }

    public String getType()
    {
        return type;
    }

    public void setFile( File file )
    {
        this.file = file;
    }

    public File getFile()
    {
        return file;
    }

    public Repository getRepository()
    {
        return repository;
    }

    public void setRepository( Repository repository )
    {
        this.repository = repository;
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public String getId()
    {
        return getDependencyConflictId() + ":" + getVersion();
    }

    public String getDependencyConflictId()
    {
        StringBuffer sb = new StringBuffer();
        sb.append( getGroupId() );
        sb.append( ":" );
        appendArtifactTypeClassifierString( sb );
        return sb.toString();
    }

    private void appendArtifactTypeClassifierString( StringBuffer sb )
    {
        sb.append( getArtifactId() );
        sb.append( ":" );
        sb.append( getType() );
        if ( hasClassifier() )
        {
            sb.append( ":" );
            sb.append( getClassifier() );
        }
    }

    // ----------------------------------------------------------------------
    // Object overrides
    // ----------------------------------------------------------------------

    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        if ( getGroupId() != null )
        {
            sb.append( getGroupId() );
            sb.append( ":" );
        }
        appendArtifactTypeClassifierString( sb );
        sb.append( ":" );
        
        if ( getVersion() != null )
        {
            sb.append( getVersion() );
        }
        
        if ( scope != null )
        {
            sb.append( ":" );
            sb.append( scope );
        }
        return sb.toString();
    }

    public int hashCode()
    {
        int result = 17;
        result = 37 * result + groupId.hashCode();
        result = 37 * result + artifactId.hashCode();
        result = 37 * result + type.hashCode();
        if ( version != null )
        {
            result = 37 * result + version.hashCode();
        }
        result = 37 * result + ( classifier != null ? classifier.hashCode() : 0 );
        return result;
    }

    public boolean equals( Object o )
    {
        if ( o == this )
        {
            return true;
        }

        if ( !( o instanceof Artifact ) )
        {
            return false;
        }

        Artifact a = (Artifact) o;

        if ( !a.getGroupId().equals( groupId ) )
        {
            return false;
        }
        else if ( !a.getArtifactId().equals( artifactId ) )
        {
            return false;
        }
        else if ( !a.getVersion().equals( version ) )
        {
            return false;
        }
        else if ( !a.getType().equals( type ) )
        {
            return false;
        }
        else if ( a.getClassifier() == null ? classifier != null : !a.getClassifier().equals( classifier ) )
        {
            return false;
        }

        // We don't consider the version range in the comparison, just the resolved version

        return true;
    }

    public int compareTo( Object o )
    {
        Artifact a = (Artifact) o;

        int result = groupId.compareTo( a.getGroupId() );
        if ( result == 0 )
        {
            result = artifactId.compareTo( a.getArtifactId() );
            if ( result == 0 )
            {
                result = type.compareTo( a.getType() );
                if ( result == 0 )
                {
                    if ( classifier == null )
                    {
                        if ( a.getClassifier() != null )
                        {
                            result = 1;
                        }
                    }
                    else
                    {
                        if ( a.getClassifier() != null )
                        {
                            result = classifier.compareTo( a.getClassifier() );
                        }
                        else
                        {
                            result = -1;
                        }
                    }
                    if ( result == 0 )
                    {
                        // We don't consider the version range in the comparison, just the resolved version
                        result = version.compareTo( a.getVersion() );
                    }
                }
            }
        }
        return result;
    }

    public String getDownloadUrl()
    {
        return downloadUrl;
    }

    public void setDownloadUrl( String downloadUrl )
    {
        this.downloadUrl = downloadUrl;
    }

    public void setScope( String scope )
    {
        this.scope = scope;
    }

    public void setGroupId( String groupId )
    {
        this.groupId = groupId;
    }

    public void setArtifactId( String artifactId )
    {
        this.artifactId = artifactId;
    }

    public void setResolved( boolean resolved )
    {
        this.resolved = resolved;
    }

    public boolean isResolved()
    {
        return resolved;
    }

    public void setResolvedVersion( String version )
    {
        this.version = version;
        // retain baseVersion
    }

    public void setRelease( boolean release )
    {
        this.release = release;
    }

    public boolean isRelease()
    {
        return release;
    }

    public boolean isOptional()
    {
        return optional;
    }

    public void setOptional( boolean optional )
    {
        this.optional = optional;
    }
}
