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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;

import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.network.ConnectionConsumer;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.registration.AbstractContinuumRegistrar;
import org.apache.maven.continuum.socket.SimpleSocket;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id$
 */
public class UrlSocketRegistrar
    extends AbstractContinuumRegistrar
    implements ConnectionConsumer
{
    /** @plexus.requirement */
    private Continuum continuum;

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

            ContinuumProjectBuildingResult result = continuum.addMavenTwoProject( url );

            for ( Iterator it = result.getProjects().iterator(); it.hasNext(); )
            {
                ContinuumProject project = (ContinuumProject) it.next();

                socket.writeLine( "id=" + project.getId() );
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
