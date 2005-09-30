package org.apache.maven.jxr.pacman;

/* ====================================================================
 *   Copyright 2001-2004 The Apache Software Foundation.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * ====================================================================
 */

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.ant.DirectoryScanner;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;


/**
 * Given a list of directories, parse them out and store them as rendered
 * packages, classes, imports, etc.
 */
public class PackageManager
{
    /**
     * Log
     */
    private static final Log LOG = LogFactory.getLog( PackageManager.class );

    private Hashtable directories = new Hashtable();

    /**
     * All the packages that have been parsed
     */
    private Hashtable packages = new Hashtable();

    /**
     * The default Java package.
     */
    private PackageType defaultPackage = new PackageType();

    /**
     * Given the name of a package (Ex: org.apache.maven.util) obtain it from
     * the PackageManager
     */
    public PackageType getPackageType( String name )
    {

        //return the default package if the name is null.
        if ( name == null )
        {
            return defaultPackage;
        }

        return (PackageType) this.packages.get( name );
    }

    /**
     * Add a package to the PackageManager
     */
    public void addPackageType( PackageType packageType )
    {
        this.packages.put( packageType.getName(), packageType );
    }

    /**
     * Get all of the packages in the PackageManager
     */
    public Enumeration getPackageTypes()
    {
        return packages.elements();
    }

    /**
     * Parse out all the directories on which this depends.
     */
    private void parse( String directory )
    {
        // Go through each directory and get the java source 
        // files for this dir.
        LOG.info( "Scanning " + directory );
        DirectoryScanner directoryScanner = new DirectoryScanner();
        File baseDir = new File( directory );
        directoryScanner.setBasedir( baseDir );
        String[] includes = {"**/*.java"};
        directoryScanner.setIncludes( includes );
        directoryScanner.scan();
        String[] files = directoryScanner.getIncludedFiles();

        for ( int j = 0; j < files.length; ++j )
        {
            LOG.debug( "parsing... " + files[j] );

            //now parse out this file to get the packages/classname/etc
            try
            {
                String fileName = new File( baseDir, files[j] ).getAbsolutePath();
                JavaFile jfi = FileManager.getInstance().getFile( fileName );

                // now that we have this parsed out blend its information
                // with the current package structure
                PackageType jp = this.getPackageType( jfi.getPackageType().getName() );

                if ( jp == null )
                {
                    this.addPackageType( jfi.getPackageType() );
                    jp = jfi.getPackageType();
                }

                //add the current class to this global package.
                if ( jfi.getClassType() != null && jfi.getClassType().getName() != null )
                {
                    jp.addClassType( jfi.getClassType() );
                }

            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }

        }

    }

    /**
     * Description of the Method
     */
    public void process( String directory )
    {
        if ( this.directories.get( directory ) == null )
        {
            this.parse( directory );
            this.directories.put( directory, directory );
        }
    }

    /**
     * Description of the Method
     */
    public void process( String[] directories )
    {

        for ( int i = 0; i < directories.length; ++i )
        {
            this.process( directories[i] );
        }

    }

    /**
     * Simple logging facility
     */
    public final static void log( String message )
    {
        System.out.println( " PackageManager -> " + message );
    }

    /**
     * Dump the package information to STDOUT. FOR DEBUG ONLY
     */
    public void dump()
    {

        LOG.debug( "Dumping out PackageManager structure" );

        Enumeration pts = this.getPackageTypes();

        while ( pts.hasMoreElements() )
        {

            //get the current package and print it.
            PackageType current = (PackageType) pts.nextElement();

            LOG.debug( current.getName() );

            //get the classes under the package and print those too.
            Enumeration classes = current.getClassTypes();

            while ( classes.hasMoreElements() )
            {

                ClassType currentClass = (ClassType) classes.nextElement();

                LOG.debug( "\t" + currentClass.getName() );

            }
        }
    }
}

