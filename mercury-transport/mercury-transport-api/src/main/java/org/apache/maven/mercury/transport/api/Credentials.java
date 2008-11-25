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
package org.apache.maven.mercury.transport.api;

/**
 * supplies credentials to the server
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class Credentials
{
  private String user;
  private String pass;
  
  private byte [] cert;
  
  public Credentials( String user, String pass )
  {
    this.user = user;
    this.pass = pass;
  }
  
  public Credentials( byte [] cert, String user, String pass )
  {
    this( user, pass );
    this.cert = cert;
  }
  
  public Credentials( byte [] cert )
  {
    this.cert = cert;
  }

  public String getUser()
  {
    return user;
  }

  public void setUser( String user )
  {
    this.user = user;
  }

  public String getPass()
  {
    return pass;
  }

  public void setPass( String pass )
  {
    this.pass = pass;
  }

  public byte [] getCertificate()
  {
    return cert;
  }
  
  public boolean isCertificate()
  {
    return cert != null && cert.length > 1;
  }

  public void setCertificate( byte [] cert )
  {
    this.cert = cert;
  }
  
  
}
