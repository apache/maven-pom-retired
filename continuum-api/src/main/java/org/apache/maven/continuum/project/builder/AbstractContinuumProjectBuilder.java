package org.apache.maven.continuum.project.builder;

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

import org.codehaus.plexus.formica.util.MungedHttpsURL;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class AbstractContinuumProjectBuilder
    extends AbstractLogEnabled
    implements ContinuumProjectBuilder
{
    protected File createMetadataFile( URL metadata, String username, String password )
        throws IOException
    {
        getLogger().info( "Downloading " + metadata.toExternalForm() );

        InputStream is = null;

        if ( metadata.getProtocol().startsWith( "http" ) )
        {
            is = new MungedHttpsURL( metadata.toExternalForm(), username, password ).getURL().openStream();
        }
        else
        {
            is = metadata.openStream();
        }

        String path = metadata.getPath();

        String baseDirectory;

        String fileName;

        int lastIndex = path.lastIndexOf( "/" );

        if ( lastIndex >= 0 )
        {
            baseDirectory = path.substring( 0, lastIndex );

            // Required for windows
            int colonIndex = baseDirectory.indexOf( ":" );

            if ( colonIndex >= 0 )
            {
                baseDirectory = baseDirectory.substring( colonIndex + 1 );
            }

            fileName = path.substring( lastIndex + 1 );
        }
        else
        {
            baseDirectory = "";

            fileName = path;
        }

        // Little hack for URLs that contains '*' like "http://svn.codehaus.org/*checkout*/trunk/pom.xml?root=plexus"
        baseDirectory = StringUtils.replace( baseDirectory, "*", "" );

        File continuumTmpDir = new File( System.getProperty( "java.io.tmpdir" ), "continuum" );

        File uploadDirectory = new File( continuumTmpDir, baseDirectory );

        uploadDirectory.mkdirs();

        FileUtils.forceDeleteOnExit( continuumTmpDir );

        File file = new File( uploadDirectory, fileName );

        file.deleteOnExit();

        FileWriter writer = new FileWriter( file );

        IOUtil.copy( is, writer );

        is.close();

        writer.close();

        return file;
    }

}
