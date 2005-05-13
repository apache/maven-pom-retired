package org.apache.maven.continuum.registration.socket;

/*
 * Copyright 2004-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.continuum.network.ConnectionConsumer;
import org.apache.maven.continuum.project.builder.maven.MavenTwoContinuumProjectBuilder;
import org.apache.maven.continuum.registration.AbstractContinuumRegistrar;
import org.apache.maven.continuum.socket.SimpleSocket;
import org.apache.maven.continuum.utils.ContinuumUtils;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id: UrlSocketRegistrar.java,v 1.1.1.1 2005/03/29 20:42:02 trygvis Exp $
 */
public class UrlSocketRegistrar
    extends AbstractContinuumRegistrar
    implements ConnectionConsumer
{
    // ----------------------------------------------------------------------
    // ConnectionConsumer Implementation
    // ----------------------------------------------------------------------

    public void consumeConnection( InputStream input, OutputStream output )
        throws IOException
    {
        SimpleSocket socket = new SimpleSocket( input, output );

        try
        {
            String url = socket.readLine();

            URL u = new URL( url );

            List ids = getContinuum().addProjectsFromUrl( u, MavenTwoContinuumProjectBuilder.ID );

            for ( Iterator it = ids.iterator(); it.hasNext(); )
            {
                String id = (String) it.next();

                socket.writeLine( "id=" + id );
            }

            socket.writeLine( "OK" );
        }
        catch( Exception ex )
        {
            socket.writeLine( "ERROR" );

            String stackTrace = getExceptionStackTrace( ex );

            socket.writeLine( "Exception while adding the project." );

            socket.writeLine( stackTrace );
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private static String getExceptionStackTrace( Throwable ex )
    {
        StringWriter string = new StringWriter();

        PrintWriter writer = new PrintWriter( string );

        ex.printStackTrace( writer );

        writer.flush();

        return string.getBuffer().toString();
    }
}
