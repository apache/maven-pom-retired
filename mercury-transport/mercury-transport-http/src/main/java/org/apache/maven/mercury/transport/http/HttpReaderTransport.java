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
package org.apache.maven.mercury.transport.http;

import org.apache.maven.mercury.spi.http.client.HttpClientException;
import org.apache.maven.mercury.spi.http.client.retrieve.DefaultRetriever;
import org.apache.maven.mercury.transport.api.AbstractTransport;
import org.apache.maven.mercury.transport.api.InitializationException;
import org.apache.maven.mercury.transport.api.ReaderTransport;
import org.apache.maven.mercury.transport.api.TransportException;
import org.apache.maven.mercury.transport.api.TransportTransaction;

/**
 * HTTP retriever adaptor: adopts DefaultRetriever to ReaderTransport API
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class HttpReaderTransport
extends AbstractTransport
implements ReaderTransport
{
  private DefaultRetriever _retriever;
  
  public TransportTransaction read( TransportTransaction trx )
  throws TransportException
  {
    return null;
  }

  public void init()
  throws InitializationException
  {
    try
    {
      _retriever = new DefaultRetriever();
    }
    catch( HttpClientException e )
    {
      throw new InitializationException(e);
    }
  }

}
