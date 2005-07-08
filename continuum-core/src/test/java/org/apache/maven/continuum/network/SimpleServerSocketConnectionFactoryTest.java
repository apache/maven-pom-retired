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

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.codehaus.plexus.PlexusTestCase;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class SimpleServerSocketConnectionFactoryTest
    extends PlexusTestCase
{
    private final static int PORT = 54321;

    private byte[] rawData = {
        (byte)0xca, (byte)0xfe, (byte)0xba, (byte)0xbe,
        (byte)0xde, (byte)0xad, (byte)0xbe, (byte)0xef
    };

    public void testBasic()
        throws Exception
    {
        ConnectionFactory factory;
        Socket socket;
        InputStream input;
        OutputStream output;
        byte[] readData;
        int i, data;

        factory = (ConnectionFactory)lookup( ConnectionFactory.ROLE );

        socket = new Socket( "127.0.0.1", PORT );

        output = socket.getOutputStream();
        input = socket.getInputStream();

        for( i = 0; i < rawData.length; i++)
        {
            output.write( rawData[i] );
        }

        readData = new byte[ rawData.length ];

        for( i = 0; i < readData.length; i++ )
        {
            data = input.read();

            if( data == -1 )
            {
                fail( "Unexpected end of stream." );
            }

            readData[ i ] = (byte)data;
        }

        assertEquals( rawData, readData );

        output.close();
        input.close();

        release( factory );
    }

    private void assertEquals( byte[] expected, byte[] actual )
    {
        int i;

        assertEquals( expected.length, actual.length );

        for( i = 0; i < expected.length; i++ )
        {
            assertEquals( "Checking byte #" + i, expected[ i ], actual[ i ] );
        }
    }
}
