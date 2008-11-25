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
package org.apache.maven.mercury.event;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.maven.mercury.event.MercuryEvent.EventMask;

/**
 *
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class DumbListener
implements MercuryEventListener
{
  
  Writer wr;
  
  public DumbListener()
  {
    this( System.out );
  }
  
  public DumbListener( OutputStream os )
  {
    wr = new OutputStreamWriter( os );
  }

  public void fire( MercuryEvent event )
  {
    try
    {
      wr.write( "mercury event: "+EventManager.toString( event )+"\n" );
      wr.flush();
    }
    catch( IOException e )
    {
      e.printStackTrace();
    }
  }

  public EventMask getMask()
  {
    return null;
  }

}
