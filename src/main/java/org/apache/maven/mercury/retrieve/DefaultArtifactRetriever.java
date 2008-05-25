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

import java.io.File;

import org.apache.maven.mercury.ArtifactRepository;
import org.apache.maven.mercury.ArtifactRepositoryLayout;
import org.apache.maven.mercury.DefaultRepositoryLayout;
import org.apache.maven.mercury.client.Binding;
import org.apache.maven.mercury.client.MercuryException;
import org.apache.maven.mercury.client.retrieve.DefaultRetrievalRequest;
import org.apache.maven.mercury.client.retrieve.DefaultRetriever;
import org.apache.maven.mercury.client.retrieve.RetrievalRequest;
import org.apache.maven.mercury.client.retrieve.RetrievalResponse;
import org.apache.maven.mercury.client.retrieve.Retriever;

/**
 * @author Jason van Zyl
 * @plexus.component
 */
public class DefaultArtifactRetriever
    implements ArtifactRetriever
{
    ArtifactRepositoryLayout layout = new DefaultRepositoryLayout();

    public ResolutionResult retrieve( ResolutionRequest request )
    {
        ResolutionResult result = new ResolutionResult();

        // Setup the HTTP client
        
        Retriever retriever;
                
        try
        {
            retriever = new DefaultRetriever();
        }
        catch ( MercuryException e )
        {
            result.addException( e );
            
            return result;
        }
                        
        for ( ArtifactRepository remoteRepository : request.getRemoteRepostories() )
        {
            RetrievalRequest rr = new DefaultRetrievalRequest();                
            String remoteUrl = remoteRepository.getUrl() + "/" + layout.pathOf( request.getArtifact() );            
            File localFile = new File( request.getLocalRepository().getBasedir(), layout.pathOf( request.getArtifact() ) );            
            Binding binding = new Binding( remoteUrl, localFile, true );            
            RetrievalResponse response = retriever.retrieve( rr );
            
            // Not found versus an error. We need to know for each repository exactly what happened.
            
            if ( !response.hasExceptions() )
            {
                // What do I want to send back to make the graph trackable
                return result;
            }            
        }
                
        return result;
    }
}
