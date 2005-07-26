package org.apache.maven.continuum.store;

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

import org.apache.maven.continuum.configuration.ConfigurationService;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class AbstractContinuumStore
    extends AbstractLogEnabled
    implements ContinuumStore
{
    /** @plexus.requirement */
    private ConfigurationService configurationService;

    public String getBuildOutput( String buildId )
        throws ContinuumStoreException
    {
        File file = getBuildOutputFile( buildId );

        try
        {
            return FileUtils.fileRead( file.getAbsolutePath() );
        }
        catch ( IOException e )
        {
            getLogger().warn( "Error reading build output for build '" + buildId + "'.", e );

            return null;
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public File getBuildOutputFile( String buildId )
        throws ContinuumStoreException
    {
        File dir = new File( configurationService.getBuildOutputDirectory(),
                             getProjectForBuild( buildId ).getId() );

        if ( !dir.exists() && !dir.mkdirs() )
        {
            throw new ContinuumStoreException( "Could not make the build output directory: " +
                                               "'" + dir.getAbsolutePath() + "'." );
        }

        return new File( dir, buildId + ".log.txt" );
    }
}
