package org.apache.maven.mercury.retrieve;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.maven.mercury.Artifact;
import org.apache.maven.mercury.ArtifactRepository;

/*
 * @author Jason van Zyl
 */
public class ResolutionResult
{
    private Artifact artifact;

    private List<ArtifactRepository> repositories;

    private List<Exception> exceptions;

    public Artifact getArtifact()
    {
        return artifact;
    }

    public void setArtifact( Artifact artifact )
    {
        this.artifact = artifact;
    }

    public List<ArtifactRepository> getRepositories()
    {
        return repositories;
    }

    public void setRepositories( List<ArtifactRepository> repositories )
    {
        this.repositories = repositories;
    }

    public List<Exception> getExceptions()
    {
        return exceptions;
    }

    public void addException( Exception e )
    {
        if ( exceptions == null )
        {
            exceptions = new ArrayList();
        }
        
        exceptions.add( e );
    }
    
    public void setExceptions( List<Exception> exceptions )
    {
        this.exceptions = exceptions;
    }

    public boolean hasExceptions()
    {
        return exceptions != null;
    }
}
