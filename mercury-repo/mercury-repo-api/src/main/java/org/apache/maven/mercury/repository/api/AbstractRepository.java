package org.apache.maven.mercury.repository.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.mercury.builder.api.MetadataProcessor;
import org.codehaus.plexus.i18n.DefaultLanguage;
import org.codehaus.plexus.i18n.Language;


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

/**
 * parent of all repositories and also a helper class for registration of readers/writers 
 * 
 * 
 */
public abstract class AbstractRepository
implements Repository
{
  private static final Language lang = new DefaultLanguage( AbstractRepository.class );
  //---------------------------------------------------------------------------
  public static final String DEFAULT_REMOTE_READ_PROTOCOL  = "http";
  public static final String DEFAULT_REMOTE_WRITE_PROTOCOL = "http";

  public static final String DEFAULT_LOCAL_READ_PROTOCOL  = "file";
  public static final String DEFAULT_LOCAL_WRITE_PROTOCOL = "file";

  public static final String DEFAULT_REPOSITORY_TYPE = "m2";

  private String             id;

  private String             defaultReadProtocol    = DEFAULT_REMOTE_READ_PROTOCOL;

  private String             defaultWriteProtocol   = DEFAULT_REMOTE_WRITE_PROTOCOL;
  //---------------------------------------------------------------------------
  private static Map< String, RepositoryReaderFactory > readerRegistry  = Collections.synchronizedMap( new HashMap<String, RepositoryReaderFactory>(4) );
  private static Map< String, RepositoryWriterFactory > writerRegistry  = Collections.synchronizedMap( new HashMap<String, RepositoryWriterFactory>(4) );
  //---------------------------------------------------------------------------
  protected String             type = DEFAULT_REPOSITORY_TYPE;
  
  protected RepositoryReader   reader;
  protected RepositoryWriter   writer;
  //---------------------------------------------------------------------------
  public AbstractRepository( String id, String type )
  {
    this.id = id;
    this.type = type;
  }
  //---------------------------------------------------------------------------
  public String getId()
  {
    return id;
  }
  //---------------------------------------------------------------------------
  public String getDefaultReadProtocol()
  {
    return defaultReadProtocol;
  }
  //---------------------------------------------------------------------------
  public void setDefaultReadProtocol(
      String defaultReadProtocol )
  {
    this.defaultReadProtocol = defaultReadProtocol;
  }
  //---------------------------------------------------------------------------
  public String getDefaultWriteProtocol()
  {
    return defaultWriteProtocol;
  }
  //---------------------------------------------------------------------------
  public void setDefaultWriteProtocol( String defaultWriteProtocol )
  {
    this.defaultWriteProtocol = defaultWriteProtocol;
  }
  //---------------------------------------------------------------------------
  public static void register( String type, RepositoryReaderFactory readerFactory )
  throws IllegalArgumentException
  {
    if( type == null || type.length() < 1 )
      throw new IllegalArgumentException( lang.getMessage( "null.reader.type" ) );
    
    if( readerFactory == null )
      throw new IllegalArgumentException( lang.getMessage( "null.reader.factory" ) );
    
    readerRegistry.put(  type, readerFactory );
  }
  //---------------------------------------------------------------------------
  public static void register( String type, RepositoryWriterFactory writerFactory )
  throws IllegalArgumentException
  {
    if( type == null || type.length() < 1 )
      throw new IllegalArgumentException( lang.getMessage( "null.writer.type" ) );
    
    if( writerFactory == null )
      throw new IllegalArgumentException( lang.getMessage( "null.writer.factory" ) );
    
    writerRegistry.put(  type, writerFactory );
  }
  //---------------------------------------------------------------------------
  public static void upregisterReader( String type )
  throws IllegalArgumentException
  {
    if( type == null || type.length() < 1 )
      throw new IllegalArgumentException( lang.getMessage( "null.reader.type" ) );
    
    readerRegistry.remove( type );
  }
  //---------------------------------------------------------------------------
  public static void upregisterWriter( String type )
  throws IllegalArgumentException
  {
    if( type == null || type.length() < 1 )
      throw new IllegalArgumentException( lang.getMessage( "null.writer.type" ) );
    
    writerRegistry.remove( type );
  }
  //---------------------------------------------------------------------------
  public static RepositoryReader getReader( String type, Repository repo, MetadataProcessor mdProcessor )
  throws IllegalArgumentException, RepositoryException
  {
    if( type == null || type.length() < 1 )
      throw new IllegalArgumentException( lang.getMessage( "null.reader.type" ) );
    
    if( repo == null )
      throw new IllegalArgumentException( lang.getMessage( "null.reader.repo" ) );
    
    RepositoryReaderFactory rf = readerRegistry.get( type );
    
    if( rf == null )
      throw new RepositoryException( lang.getMessage( "null.reader.factory.found" ) );
    
    return rf.getReader( repo, mdProcessor );
  }
  //---------------------------------------------------------------------------
  public static RepositoryWriter getWriter( String type, Repository repo, MetadataProcessor mdProcessor )
  throws IllegalArgumentException, RepositoryException
  {
    if( type == null || type.length() < 1 )
      throw new IllegalArgumentException( lang.getMessage( "null.writer.type" ) );
    
    if( repo == null )
      throw new IllegalArgumentException( lang.getMessage( "null.writer.repo" ) );
    
    RepositoryWriterFactory wf = writerRegistry.get( type );
    
    if( wf == null )
      throw new RepositoryException( lang.getMessage( "null.writer.factory.found" ) );
    
    return wf.getWriter( repo, mdProcessor );
  }
  //---------------------------------------------------------------------------
  //---------------------------------------------------------------------------
}
