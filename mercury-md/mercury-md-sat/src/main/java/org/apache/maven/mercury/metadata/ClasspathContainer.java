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
package org.apache.maven.mercury.metadata;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.mercury.artifact.ArtifactMetadata;
import org.apache.maven.mercury.artifact.ArtifactScopeEnum;

/*
 * classpath container that is aware of the classpath scope
 * 
 * @author <a href="oleg@codehaus.org">Oleg Gusakov</a>
 */
public class ClasspathContainer
    implements Iterable<ArtifactMetadata>
{
    private List<ArtifactMetadata> classpath;

    private ArtifactScopeEnum scope;

    //-------------------------------------------------------------------------------------------
    public ClasspathContainer( ArtifactScopeEnum scope )
    {
        this.scope = ArtifactScopeEnum.checkScope( scope );
    }

    //-------------------------------------------------------------------------------------------
    public ClasspathContainer( List<ArtifactMetadata> classpath, ArtifactScopeEnum scope )
    {
        this( scope );
        this.classpath = classpath;
    }

    //-------------------------------------------------------------------------------------------
    public Iterator<ArtifactMetadata> iterator()
    {
        return classpath == null ? null : classpath.iterator();
    }

    //-------------------------------------------------------------------------------------------
    public ClasspathContainer add( ArtifactMetadata md )
    {
        if ( classpath == null )
        {
            classpath = new ArrayList<ArtifactMetadata>( 16 );
        }

        classpath.add( md );

        return this;
    }

    //-------------------------------------------------------------------------------------------
    public List<ArtifactMetadata> getClasspath()
    {
        return classpath;
    }

    //-------------------------------------------------------------------------------------------
    public MetadataTreeNode getClasspathAsTree()
    {
        if ( classpath == null || classpath.size() < 1 )
            return null;

        MetadataTreeNode tree = null;
        MetadataTreeNode parent = null;
        MetadataTreeNode node = null;

        for ( ArtifactMetadata md : classpath )
        {
          // TODO Oleg: is null for query good here ??
            node = new MetadataTreeNode( md, parent, null, md.isResolved() );
            
            if ( tree == null )
            {
                tree = node;
            }

            if ( parent != null )
            {
                parent.addChild( node );
            }

            parent = node;

        }
        return tree;
    }

    public void setClasspath( List<ArtifactMetadata> classpath )
    {
        this.classpath = classpath;
    }

    public ArtifactScopeEnum getScope()
    {
        return scope;
    }

    public void setScope( ArtifactScopeEnum scope )
    {
        this.scope = scope;
    }

    //-------------------------------------------------------------------------------------------
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder( 256 );
        sb.append( "[scope=" + scope.getScope() );
        if ( classpath != null )
            for ( ArtifactMetadata md : classpath )
            {
                sb.append( ": " + md.toString() + '{' + md.getArtifactUri() + '}' );
            }
        sb.append( ']' );
        return sb.toString();
    }
    //-------------------------------------------------------------------------------------------
    //-------------------------------------------------------------------------------------------
}
