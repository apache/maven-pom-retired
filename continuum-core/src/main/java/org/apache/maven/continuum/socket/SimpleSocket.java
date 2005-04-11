package org.apache.maven.continuum.socket;

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
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import org.codehaus.plexus.util.IOUtil;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id: SimpleSocket.java,v 1.1.1.1 2005/03/29 20:42:02 trygvis Exp $
 */
public class SimpleSocket
{
    private Socket socket;

    private InputStream input;

    private BufferedReader reader;

    private OutputStream output;

    private PrintWriter writer;

    public SimpleSocket( String host, int port )
        throws UnknownHostException, IOException
    {
        socket = new Socket( host, port );

        setup( socket.getInputStream(), socket.getOutputStream() );
    }

    public SimpleSocket( InputStream input, OutputStream output )
        throws IOException
    {
        if ( input == null )
        {
            throw new IllegalArgumentException( "input cannot be null" );
        }

        if ( output == null )
        {
            throw new IllegalArgumentException( "output cannot be null" );
        }

        setup( input, output );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public String readLine()
        throws IOException
    {
        return reader.readLine();
    }

    public void writeLine( String line )
        throws IOException
    {
        writer.println( line );

        writer.flush();

        output.flush();
    }

    public void close()
    {
        IOUtil.close( reader );
        IOUtil.close( writer );
        IOUtil.close( input );
        IOUtil.close( output );

        if ( socket != null )
        {
            try
            {
                socket.close();
            }
            catch ( IOException e )
            {
                // ignore
            }
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void setup( InputStream input, OutputStream output )
        throws IOException
    {
        this.input = input;

        this.output = output;

        reader = new BufferedReader( new InputStreamReader( input ) );

        writer = new PrintWriter( new OutputStreamWriter( output ) );
    }
}
