package org.apache.maven.continuum.trigger.socket;

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.network.ConnectionConsumer;
import org.apache.maven.continuum.trigger.AbstractContinuumTrigger;

import org.codehaus.plexus.util.IOUtil;

/**
 * This trigger listens on a specified port and takes one line
 * of input which contains the the groupId and artifactId of the
 * project to build or the special word "all" to indicate building
 * all the projects.
 *
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id: SimpleSocketTrigger.java,v 1.1.1.1 2005/03/29 20:42:03 trygvis Exp $
 */
public class SimpleSocketTrigger
    extends AbstractContinuumTrigger
    implements ConnectionConsumer
{
    // ----------------------------------------------------------------------
    // ConnectionConsumer Implementation
    // ----------------------------------------------------------------------

    public void consumeConnection( InputStream input, OutputStream output )
        throws IOException
    {
        PrintWriter printer = new PrintWriter( output );

        BufferedReader reader = new BufferedReader( new InputStreamReader( input ) );

        String id;

        try
        {
            id = reader.readLine();

            if ( id.length() == 0 )
            {
                out( printer, "ERROR" );

                out( printer, "Error in input, expected format: id." );

                return;
            }

            String buildId = getContinuum().buildProject( id );

            out( printer, "OK" );

            out( printer, "id=" + buildId );

            out( printer, "Build of " + id + " scheduled." );
        }
        catch ( ContinuumException ex )
        {
            out( printer, "ERROR" );

            ex.printStackTrace( printer );
        }
        finally
        {
            printer.flush();

            IOUtil.close( printer );
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void out( PrintWriter printer, String line )
    {
        printer.println( line );

        printer.flush();

        getLogger().info( line );
    }
}
