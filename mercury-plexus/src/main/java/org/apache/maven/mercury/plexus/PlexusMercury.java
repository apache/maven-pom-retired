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
package org.apache.maven.mercury.plexus;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.maven.mercury.artifact.Artifact;
import org.apache.maven.mercury.artifact.ArtifactExclusionList;
import org.apache.maven.mercury.artifact.ArtifactInclusionList;
import org.apache.maven.mercury.artifact.ArtifactMetadata;
import org.apache.maven.mercury.artifact.ArtifactQueryList;
import org.apache.maven.mercury.artifact.ArtifactScopeEnum;
import org.apache.maven.mercury.builder.api.DependencyProcessor;
import org.apache.maven.mercury.crypto.api.StreamObserverFactory;
import org.apache.maven.mercury.crypto.api.StreamVerifierException;
import org.apache.maven.mercury.crypto.api.StreamVerifierFactory;
import org.apache.maven.mercury.crypto.pgp.PgpStreamVerifierFactory;
import org.apache.maven.mercury.repository.api.Repository;
import org.apache.maven.mercury.repository.api.RepositoryException;
import org.apache.maven.mercury.repository.local.m2.LocalRepositoryM2;
import org.apache.maven.mercury.repository.remote.m2.RemoteRepositoryM2;

/**
 * 
 * @author Oleg Gusakov
 * 
 */
public interface PlexusMercury
{
  public static String ROLE = PlexusMercury.class.getName();

  /**
   * lookup dependency processor in plexus and return the "default" implementation
   * 
   * @return the dependency processor
   * @throws RepositoryException
   */
  public DependencyProcessor findDependencyProcessor()
  throws RepositoryException
  ;

  /**
   * lookup dependency processor in plexus and return the requested implementation
   * 
   * @param hint the plexus hint to lookup by
   * @return the dependency processor
   * @throws RepositoryException
   */
  public DependencyProcessor findDependencyProcessor( String hint )
  throws RepositoryException
  ;
  
  /**
   * create PGP factory to configure into repository reader for signature verification
   * 
   * @param lenient
   * @param sufficient
   * @param pubRing - keyring with all acceptable public keys
   * @return pgp verifier factory to be sent to Repository
   * @throws PlexusMercuryException
   */
  public PgpStreamVerifierFactory createPgpReaderFactory( boolean lenient, boolean sufficient, InputStream pubRing )
  throws StreamVerifierException;

  /**
   * create PGP factory to configure into repository writer for signature generation
   * 
   * @param lenient
   * @param sufficient
   * @param secRing
   * @param keyId
   * @param keyPass
   * @return pgp verifier factory to be sent to Repository
   * @throws PlexusMercuryException
   */
  public PgpStreamVerifierFactory createPgpWriterFactory( 
                      boolean lenient
                    , boolean sufficient
                    , InputStream secRing
                    , String keyId
                    , String keyPass
                                                        )
  throws StreamVerifierException;
  
  /**
   * construct remote M2 repository and configure it with supplied attributes
   * 
   * @param id
   * @param serverUrl
   * @param serverUser
   * @param serverPass
   * @param proxyUrl
   * @param proxyUser
   * @param proxyPass
   * @param readerStreamObservers
   * @param readerStreamVerifiers
   * @param writerStreamObservers
   * @param writerStreamVerifiers
   * @return repository instance
   * @throws PlexusMercuryException
   */
  public RemoteRepositoryM2 constructRemoteRepositoryM2(
      String id
    , URL serverUrl, String serverUser, String serverPass 
    , URL proxyUrl,  String proxyUser,  String proxyPass
    , Set<StreamObserverFactory> readerStreamObservers
    , Set<StreamVerifierFactory> readerStreamVerifiers
    , Set<StreamObserverFactory> writerStreamObservers
    , Set<StreamVerifierFactory> writerStreamVerifiers
                                     )
  throws RepositoryException;

  
  /**
   * construct local M2 repository and configure it with supplied attributes
   * 
   * @param id
   * @param rootDir
   * @param readerStreamObservers
   * @param readerStreamVerifiers
   * @param writerStreamObservers
   * @param writerStreamVerifiers
   * @return repository instance
   * @throws PlexusMercuryException
   */
  public LocalRepositoryM2 constructLocalRepositoryM2(
      String id
    , File rootDir
    , Set<StreamObserverFactory> readerStreamObservers
    , Set<StreamVerifierFactory> readerStreamVerifiers
    , Set<StreamObserverFactory> writerStreamObservers
    , Set<StreamVerifierFactory> writerStreamVerifiers
                                     )
  throws RepositoryException;

  /**
   * write (deploy) given Artifact(s) to the repository
   * 
   * @param repo repository instance to search
   * @param artfifacts to write
   * @return
   * @throws PlexusMercuryException
   */
  public void write( Repository repo, Artifact... artifacts )
  throws RepositoryException;
  public void write( Repository repo, Collection<Artifact> artifacts )
  throws RepositoryException;

  /**
   * read given Artifact(s) from the repository
   * 
   * @param repo repository instance to search
   * @param artfifacts to read
   * @return
   * @throws PlexusMercuryException
   */
  public List<Artifact> read( List<Repository> repo, List<ArtifactMetadata> artifacts )
  throws RepositoryException;
  public List<Artifact> read( List<Repository> repo, ArtifactMetadata... artifacts )
  throws RepositoryException;

  /**
   * resolve Artifact dependencies. The inclusions and exclusions accept version ranges as parameters
   * 
   * @param repo repository instance to search
   * @param scope scope enumeration member indication the scope to resolve for
   * @param artfifacts list of artifact metadatas to resolve
   * @param inclusions list of artifact metadatas to include - only these will be in the resolved classpath
   * @param exclusions list of artifact metadatas to exclude - is applied after the inclusion, if one is present
   * @return
   * @throws PlexusMercuryException
   */
  public List<ArtifactMetadata> resolve( List<Repository> repos
                                      , ArtifactScopeEnum   scope
                                      , ArtifactQueryList artifacts
                                      , ArtifactInclusionList inclusions
                                      , ArtifactExclusionList exclusions
                                      )
  throws RepositoryException;
  
  public List<ArtifactMetadata> resolve( List<Repository> repos, ArtifactScopeEnum scope, ArtifactMetadata metadata )
  throws RepositoryException;  
  
}
