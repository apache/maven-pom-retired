package org.apache.maven.jxr.pacman;

/*
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;

/**
 * PacMan implementation of a JavaFile. This will parse out the file and
 * determine package, class, and imports
 *
 * @author <a href="mailto:burton@apache.org">Kevin A. Burton</a>
 * @version $Id$
 */
public class JavaFileImpl
    extends JavaFile
{
    private Reader reader;

    /**
     * Create a new JavaFileImpl that points to a given file...
     *
     * @param filename
     * @throws IOException
     */
    public JavaFileImpl( String filename, String encoding )
        throws IOException
    {
        this.setFilename( filename );
        this.setEncoding( encoding );

        //always add java.lang.* to the package imports because the JVM always
        //does this implicitly.  Unless we add this to the ImportTypes JXR
        //won't pick up on this.

        this.addImportType( new ImportType( "java.lang.*" ) );

        //now parse out this file.

        this.parse();
    }

    /**
     * Open up the file and try to determine package, class and import
     * statements.
     */
    private void parse()
        throws IOException
    {
        StreamTokenizer stok = null;
        try
        {
            stok = this.getTokenizer();

            while ( stok.nextToken() != StreamTokenizer.TT_EOF )
            {

                if ( stok.sval == null )
                {
                    continue;
                }

                //set the package
                if ( stok.sval.equals( "package" ) )
                {
                    stok.nextToken();
                    this.setPackageType( new PackageType( stok.sval ) );
                }

                //set the imports
                if ( stok.sval.equals( "import" ) )
                {
                    stok.nextToken();

                    String name = stok.sval;

                    /*
                    WARNING: this is a bug/non-feature in the current
                    StreamTokenizer.  We needed to set the comment char as "*"
                    and packages that are imported with this (ex "test.*") will be
                    stripped( and become "test." ).  Here we need to test for this
                    and if necessary re-add the char.
                    */
                    if ( name.charAt( name.length() - 1 ) == '.' )
                    {
                        name = name + "*";
                    }

                    this.addImportType( new ImportType( name ) );
                }

                //set the Class... if the class is found no more information is
                //valid so just break out of the while loop at this point.
                //set the imports
                if ( stok.sval.equals( "class" ) || stok.sval.equals( "interface" ) || stok.sval.equals( "enum" ) )
                {
                    stok.nextToken();
                    this.setClassType( new ClassType( stok.sval ) );
                    break;
                }

            }
        }
        finally
        {
            stok = null;
            if ( this.reader != null )
            {
                this.reader.close();
            }
        }
    }

    /**
     * Get a StreamTokenizer for this file.
     */
    private StreamTokenizer getTokenizer()
        throws IOException
    {

        if ( !new File( this.getFilename() ).exists() )
        {
            throw new IOException( this.getFilename() + " does not exist!" );
        }

        if ( this.getEncoding() != null )
        {
            this.reader = new InputStreamReader( new FileInputStream( this.getFilename() ), this.getEncoding() );
        }
        else
        {
            this.reader = new FileReader( this.getFilename() );
        }

        StreamTokenizer stok = new StreamTokenizer( reader );
        //int tok;

        stok.commentChar( '*' );
        stok.wordChars( '_', '_' );

        // set tokenizer to skip comments
        stok.slashStarComments( true );
        stok.slashSlashComments( true );

        return stok;
    }

}
