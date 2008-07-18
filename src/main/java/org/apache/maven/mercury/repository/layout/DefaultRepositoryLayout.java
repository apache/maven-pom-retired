package org.apache.maven.mercury.repository.layout;

import org.apache.maven.mercury.Artifact;
import org.apache.maven.mercury.metadata.ArtifactMetadata;

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

/**
 * @author jdcasey
 * @author Oleg Gusakov
 * 
 * @plexus.component role="org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout" role-hint="default"
 */
public class DefaultRepositoryLayout
implements RepositoryLayout
{
    private static final char PATH_SEPARATOR = '/';

    private static final char GROUP_SEPARATOR = '.';

    private static final char ARTIFACT_SEPARATOR = '-';

    public String pathOf( ArtifactMetadata md )
    {
        StringBuffer path = new StringBuffer();

        path.append( formatAsDirectory( md.getGroupId() ) ).append( PATH_SEPARATOR );
        path.append( md.getArtifactId() ).append( PATH_SEPARATOR );
        path.append( md.getVersion() ).append( PATH_SEPARATOR );
        path.append( md.getArtifactId() ).append( ARTIFACT_SEPARATOR ).append( md.getVersion() );

        if ( md.hasClassifier() )
        {
            path.append( ARTIFACT_SEPARATOR ).append( md.getClassifier() );
        }

        path.append( GROUP_SEPARATOR ).append( "pom" );
        
        /*
        if ( artifactHandler.getExtension() != null && artifactHandler.getExtension().length() > 0 )
        {
            path.append( GROUP_SEPARATOR ).append( artifactHandler.getExtension() );
        }
        */

        return path.toString();
    }

    private String formatAsDirectory( String directory )
    {
        return directory.replace( GROUP_SEPARATOR, PATH_SEPARATOR );
    }

}
