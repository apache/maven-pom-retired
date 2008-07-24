package org.apache.maven.mercury.repository;


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
 * parent of all repositories
 */
public abstract class AbstractRepository
implements Repository
{
  public static final String DEFAULT_READ_PROTOCOL  = "http";
  public static final String DEFAULT_WRITE_PROTOCOL = "http";

  public static final String DEFAULT_LOCAL_READ_PROTOCOL  = "file";
  public static final String DEFAULT_LOCAL_WRITE_PROTOCOL = "file";

  private String             id;

  private String             defaultReadProtocol    = DEFAULT_READ_PROTOCOL;

  private String             defaultWriteProtocol   = DEFAULT_WRITE_PROTOCOL;

  public AbstractRepository( String id )
  {
    this.id = id;
  }

  public String getId()
  {
    return id;
  }

  public String getDefaultReadProtocol()
  {
    return defaultReadProtocol;
  }

  public void setDefaultReadProtocol(
      String defaultReadProtocol )
  {
    this.defaultReadProtocol = defaultReadProtocol;
  }

  public String getDefaultWriteProtocol()
  {
    return defaultWriteProtocol;
  }

  public void setDefaultWriteProtocol(
      String defaultWriteProtocol )
  {
    this.defaultWriteProtocol = defaultWriteProtocol;
  }

}
