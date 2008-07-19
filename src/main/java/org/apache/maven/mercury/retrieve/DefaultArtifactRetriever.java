package org.apache.maven.mercury.retrieve;

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

import org.apache.maven.mercury.repository.RemoteRepository;
import org.apache.maven.mercury.spi.http.client.Binding;
import org.apache.maven.mercury.spi.http.client.MercuryException;
import org.apache.maven.mercury.spi.http.client.retrieve.DefaultRetrievalRequest;
import org.apache.maven.mercury.spi.http.client.retrieve.DefaultRetriever;
import org.apache.maven.mercury.spi.http.client.retrieve.RetrievalResponse;
import org.apache.maven.mercury.spi.http.client.retrieve.Retriever;

/**
 * @author Jason van Zyl
 * 
 * @plexus.component
 */
public class DefaultArtifactRetriever
implements ArtifactRetriever
{
    public ResolutionResult retrieve( ResolutionRequest request )
    {
        ResolutionResult result = new ResolutionResult();

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

        for ( RemoteRepository remoteRepository : request.getRemoteRepostories() )
        {
            DefaultRetrievalRequest rr = new DefaultRetrievalRequest();
            String remoteUrl = remoteRepository.getUrl() + "/" + remoteRepository.pathOf( request.getMd() );
            File localFile = new File( request.getLocalRepository().getDirectory(), request.getLocalRepository().pathOf( request.getMd() ) );
            Binding binding = new Binding( remoteUrl, localFile, true );
            rr.addBinding( binding );
            RetrievalResponse response = retriever.retrieve( rr );

            // Not found versus an error. We need to know for each repository exactly what happened.

            if ( !response.hasExceptions() )
            {
                // Specify the repository where the artifact was actually found.
                result.setRemoteRepository( remoteRepository );

                // Would be also good to collect some metrics about how long it took to retrieve.
                // result.setRetrievalTime( response.getRetrievalTime() );    

                return result;
            }
            else
            {
                for( Exception exception: response.getExceptions() )
                {                    
                    System.out.println(exception);
                }
            }
                        
            // We have to know when we didn't get anything.
        }
        
        return result;
    }
}
