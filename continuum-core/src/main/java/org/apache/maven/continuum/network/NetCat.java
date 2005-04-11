package org.apache.maven.continuum.network;

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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringInputStream;

/**
 * This class emulates the unix tool <code>nc</code>.
 *
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id: NetCat.java,v 1.1.1.1 2005/03/29 20:42:01 trygvis Exp $
 */
public class NetCat
{
    private static int defaultBufferSize = 4096;

    private NetCat()
    {
    }

    public static String write( String host, int port, String contents )
        throws IOException
    {
        return write( host, port, new StringInputStream( contents ) );
    }

    public static String write( String host, int port, InputStream contents )
        throws IOException
    {
        OutputStream output = null;
        InputStream input;
        Socket socket = null;
        final byte[] buffer = new byte[defaultBufferSize];
        int n = 0;
        ByteArrayOutputStream result = new ByteArrayOutputStream();

        try
        {
            socket = new Socket( host, port );
            output = socket.getOutputStream();
            input = socket.getInputStream();

            while ( ( n = contents.read( buffer ) ) != -1 )
            {
//                for( int i = 0; i < n; i++)
//                    System.err.print( (char)buffer[i] );
                output.write( buffer, 0, n );
            }

            System.err.println( "Reading return value" );

            while ( ( n = input.read( buffer ) ) != -1 )
            {
//                for( int i = 0; i < n; i++)
//                    System.err.print( (char)buffer[i] );
                result.write( buffer, 0, n );
            }
        }
        finally
        {
            IOUtil.close( output );

            NetworkUtils.closeSocket( socket );
        }

        return result.toString();
    }
}
