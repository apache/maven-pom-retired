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

import java.util.Collection;

/**
 * generic Transport interface - allows client to read data from a remote repository
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public interface ReaderTransport
extends Initializable
{
  public TransportTransaction read( TransportTransaction trx )
  throws TransportException;
  
  public void setServers( Collection<Server> servers );
  
  public Collection<Server> getServers();
}
