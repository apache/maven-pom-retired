/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file                                                                                            
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.maven.mercury.spi.http.client;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.maven.mercury.crypto.api.StreamObserver;



public class ObservableInputStream extends FilterInputStream
{
    Set<StreamObserver> observers = new HashSet<StreamObserver>();

    public ObservableInputStream(InputStream in)
    {
        super(in);
    }

    public int read(byte[] b, int off, int len) throws IOException
    {
        int result = in.read(b, off, len);
        if (result != -1) 
        {
            notifyListeners(b, off, result);
        }
        return result;
    }

   
    public int read() throws IOException
    {
        int ch = in.read();
        if (ch != -1) 
        {
            notifyListeners(ch);
        }
        return ch;
    }

    
    public void addObserver (StreamObserver o)
    {
        synchronized (this.observers)
        {
            this.observers.add(o);
        }
    }
    
    public void addObservers (Collection<? extends StreamObserver> observers)
    {
        synchronized (this.observers)
        {
            this.observers.addAll(observers);
        }
    }
    
    private void notifyListeners (byte[]b, int off, int len)
    {
        synchronized (this.observers)
        {
            for (StreamObserver o: this.observers)
            {
                o.bytesReady(b, off, len);
            }
        }
    }

    private void notifyListeners (int b)
    {
        synchronized (this.observers)
        {
            for (StreamObserver o: this.observers)
            {
                o.byteReady(b);
            }
        }
    } 
   
}
