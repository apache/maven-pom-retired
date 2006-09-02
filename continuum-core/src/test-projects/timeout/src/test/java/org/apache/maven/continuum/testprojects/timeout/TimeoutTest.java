package projects.timeout.src.test.java.org.apache.maven.continuum.testprojects.timeout;

/*
 * Copyright 2006 The Apache Software Foundation.
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

import java.io.File;
import java.io.FileOutputStream;

import junit.framework.TestCase;

/**
 * This is a 'long running' test, with pre/post file events, allowing
 * Continuum to test aborting tasks.
 *
 * @author <a href="mailto:kenney@apache.org">Kenney Westerhof</a>
 */
public class TimeoutTest
    extends TestCase
{
    public void testTimeout()
        throws Exception
    {
        deleteFile( "target/TEST-COMPLETED" );
        createFile( "target/TEST-STARTED", "Test started" );

        System.err.println( "Sleeping for 15 seconds." );

        Thread.sleep( 15000 );

        createFile( "target/TEST-COMPLETED", "Test completed" );
    }

    private static void createFile( String filename, String content )
        throws Exception
    {
        FileOutputStream fout = new FileOutputStream( filename );
        fout.write( content.getBytes() );
        fout.close();
    }

    private static void deleteFile( String filename )
        throws Exception
    {
        new File( filename ).delete();
    }
}
