package org.apache.maven.mercury.artifact;

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
import java.io.InputStream;
import java.util.regex.Pattern;


/*
 * Description of an artifact.
 * 
 * @todo do we really need an interface here?
 * 
 * @todo get rid of the multiple states we can have (project, parent, etc artifacts, file == null,
 * snapshot, etc) - construct subclasses and use accordingly?
 */
public interface Artifact
extends Comparable<Artifact>
{
    String LATEST_VERSION = "LATEST";

    String SNAPSHOT_VERSION = "SNAPSHOT";

    String RELEASE_VERSION = "RELEASE";

    Pattern VERSION_FILE_PATTERN = Pattern.compile( "^(.*)-([0-9]{8}.[0-9]{6})-([0-9]+)$" );

    // TODO: into artifactScope handler

    String SCOPE_COMPILE = ArtifactScopeEnum.compile.toString();

    String SCOPE_TEST = ArtifactScopeEnum.test.toString();

    String SCOPE_RUNTIME = ArtifactScopeEnum.runtime.toString();

    String SCOPE_PROVIDED = ArtifactScopeEnum.provided.toString();

    String SCOPE_SYSTEM = ArtifactScopeEnum.system.toString();

    String getGroupId();

    String getArtifactId();

    String getVersion();

    void setVersion( String version );

    /**
     * Get the artifactScope of the artifact. If the artifact is a standalone rather than a dependency, it's artifactScope will be
     * <code>null</code>. The artifactScope may not be the same as it was declared on the original dependency, as this is the
     * result of combining it with the main project artifactScope.
     *
     * @return the artifactScope
     */
    String getScope();

    String getType();

    String getClassifier();

    // only providing this since classifier is *very* optional...
    boolean hasClassifier();

    File getFile();
    
    InputStream getStream();

    void setFile( File destination );
    
    byte [] getPomBlob();
    
    String getBaseName();
    
    String getBaseName( String classifier );

    // ----------------------------------------------------------------------

    String getId();

    void setGroupId( String groupId );

    void setArtifactId( String artifactId );
    
    boolean isResolved();
    
    void setResolved( boolean resolved );
}