package org.apache.maven.continuum.web.action;

/*
 * Copyright 2001-2006 The Apache Software Foundation.
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

import org.codehaus.plexus.PlexusTestCase;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * TestContinuumActionLogging:
 *
 * @author: jesse
 * @date: Jul 13, 2006
 * @version: $ID$
 */
public class TestContinuumActionLogging
    extends PlexusTestCase
{

    StringBuffer testOutput = new StringBuffer();


    public void setUp() throws Exception
    {
        super.setUp();

        PrintStream systemPrintStream = new PrintStream( new FilteredStream( System.out ), true );
        System.setOut( systemPrintStream );
    }


    public void tearDown()
    {
        System.setOut( new PrintStream(
            new BufferedOutputStream( new FileOutputStream( java.io.FileDescriptor.out ), 128 ), true ) );
    }


    public void testActionLogging()
    {
        try
        {
            TestAction testAction = (TestAction) lookup( "com.opensymphony.xwork.Action", "testAction" );
            String testString = "action test string";
            testAction.setTestString( testString );

            testAction.execute();

            assertTrue( testOutput.toString().indexOf( testString ) != -1 );
        }
        catch ( Exception e )
        {
            fail( e.getMessage() );
        }
    }


    class FilteredStream
        extends FilterOutputStream
    {
        OutputStream stream;

        public FilteredStream( OutputStream stream )
        {
            super( stream );
            this.stream = stream;
        }

        public void write( byte byteArray[] )
            throws IOException
        {
            testOutput.append( new String ( byteArray ) );
            stream.write( byteArray );
        }

        public void write( byte byteArray[], int offset, int length )
            throws IOException
        {
            testOutput.append( new String( byteArray, offset, length ) );
            stream.write( byteArray, offset, length );
        }
    }
}
