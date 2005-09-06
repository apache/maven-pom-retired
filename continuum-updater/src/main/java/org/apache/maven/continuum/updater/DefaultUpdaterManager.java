package org.apache.maven.continuum.updater;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
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

import org.apache.maven.continuum.updater.exception.UpdaterException;
import org.apache.maven.continuum.updater.model.UpdaterModel;
import org.apache.maven.continuum.updater.model.Version;
import org.apache.maven.continuum.updater.model.io.xpp3.ContinuumUpdaterXpp3Reader;
import org.codehaus.plexus.logging.AbstractLogEnabled;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.File;
import java.util.Iterator;
import java.util.Map;

/**
 * @plexus.component
 *   role="org.apache.maven.continuum.updater.UpdaterManager"
 *
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class DefaultUpdaterManager
    extends AbstractLogEnabled
    implements UpdaterManager
{
    /**
     * @plexus.requirement
     *   role="org.apache.maven.continuum.updater.Updater"
     */
    private Map updaters;

    public void execute( String version, File continuumHome )
        throws UpdaterException
    {
        InputStream confAsStream;

        confAsStream = DefaultUpdaterManager.class.getClassLoader().getResourceAsStream( "updater.xml" );

        InputStreamReader reader = new InputStreamReader( confAsStream );

        ContinuumUpdaterXpp3Reader xpp3Reader = new ContinuumUpdaterXpp3Reader();

        UpdaterModel model = null;

        try
        {
            model = xpp3Reader.read( reader );
        }
        catch ( Exception e )
        {
            throw new UpdaterException( "Can't find updater configuration file.", e );
        }

        boolean versionFounded = false;

        for( Iterator i = model.getVersions().iterator(); i.hasNext(); )
        {
            Version v = (Version) i.next();

            if ( v.getName().equals( version ) )
            {
                versionFounded = true;
            }

            if ( versionFounded && v.getComponentRole() != null )
            {
                update( v, continuumHome );
            }
        }

        if ( !versionFounded )
        {
            getLogger().warn( "There are no updater for your version(" + version + ")\n" + getAvailableVersions( model ) );
        }
    }

    private String getAvailableVersions( UpdaterModel model )
    {
        StringBuffer sb = new StringBuffer();

        for( Iterator i = model.getVersions().iterator(); i.hasNext(); )
        {
            Version v = (Version) i.next();

            if ( v.getComponentRole() != null )
            {
                if ( sb.length() != 0 )
                {
                    sb.append( "\n" );
                }

                sb.append( v.getName() );
            }
        }

        return sb.toString();
    }

    private void update( Version version, File continuumHome )
        throws UpdaterException
    {
        getLogger().info( "************************************************************************" );
        getLogger().info( "Update version " + version.getName() + " with component " + version.getComponentRole() );
        getLogger().info( "************************************************************************" );

        Updater updater = (Updater) updaters.get( version.getComponentRole() );

        getLogger().info( "Update database" );

        updater.updateDatabase();

        getLogger().info( "Update librairies" );

        updater.updateLibraries();

        getLogger().info( "Update file system" );

        updater.updateFileSystem();
    }
}
