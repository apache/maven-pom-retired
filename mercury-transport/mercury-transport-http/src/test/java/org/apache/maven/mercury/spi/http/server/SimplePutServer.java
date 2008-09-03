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

package org.apache.maven.mercury.spi.http.server;

import java.io.File;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.handler.HandlerCollection;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.DefaultServlet;
import org.mortbay.jetty.servlet.FilterHolder;
import org.mortbay.servlet.PutFilter;

public class SimplePutServer extends Server
{
    protected File _base;
    protected Context context;
    
    
    public SimplePutServer()
    throws Exception
    {
      this("/maven2/repo", null );
    }
    
    /**
     * @param string
     * @param targetDirectory
     */
    public SimplePutServer( String contextPath, File targetDirectory )
    throws Exception
    {
      super(0);

      HandlerCollection handlers = new HandlerCollection();
      setHandler(handlers);

      context = new Context( handlers, contextPath );
      handlers.addHandler(new DefaultHandler());

      if( targetDirectory == null )
      {
        _base = File.createTempFile("simplePutServer",null);
        _base.delete();
        _base.mkdir();
        _base.deleteOnExit();
      }
      else
      {
        _base = targetDirectory;
      }
      
      if( _base == null || !_base.exists() || !_base.isDirectory() )
        throw new Exception("File not appropriate for base directory: "+_base);

      FilterHolder holder = context.addFilter(PutFilter.class, "/*", 0);
      holder.setInitParameter("delAllowed","true");
      context.addServlet(DefaultServlet.class,"/");
      context.setResourceBase(_base.getCanonicalPath());
    }

    public void destroy ()
    {
        super.destroy();
        destroy(_base);
    }
    
    public void destroy (File f)
    {
        if (f == null)
            return;
        if (f.isDirectory())
        {
            File[] files = f.listFiles();
            for (int i=0;files!=null && i<files.length; i++)
            {
                destroy (files[i]);
            }  
        }
        f.delete(); 
    }
    
  
    
    /* (non-Javadoc)
     * @see org.apache.maven.mercury.spi.http.server.PutServer#getPutDir()
     */
    public File getPutDir ()
    {
        return _base;
    }

    /* (non-Javadoc)
     * @see org.apache.maven.mercury.spi.http.server.PutServer#getPort()
     */
    public int getPort()
    {
        return getConnectors()[0].getLocalPort();
    }

    public static void main(String[] args)
    throws Exception
    {
        SimplePutServer server = new SimplePutServer();
        server.start();
        server.join();
    }

}
