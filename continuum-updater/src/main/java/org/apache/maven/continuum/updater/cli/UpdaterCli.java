package org.apache.maven.continuum.updater.cli;

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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.maven.continuum.updater.exception.UpdaterException;
import org.apache.maven.continuum.updater.UpdaterManager;
import org.codehaus.classworlds.ClassWorld;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.embed.Embedder;

import java.io.File;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class UpdaterCli
{
    public static int main( String[] args, ClassWorld classWorld )
    {
        File continuumHome;

        String version;

        // ----------------------------------------------------------------------
        // Setup the command line parser
        // ----------------------------------------------------------------------

        CLIManager cliManager = new CLIManager();

        CommandLine commandLine;

        try
        {
            commandLine = cliManager.parse( args );
        }
        catch ( ParseException e )
        {
            System.err.println( "Unable to parse command line options: " + e.getMessage() );

            cliManager.displayHelp();

            return 1;
        }

        // ----------------------------------------------------------------------
        // Process command line options
        // ----------------------------------------------------------------------

        if ( commandLine.hasOption( CLIManager.HELP ) )
        {
            cliManager.displayHelp();

            return 0;
        }

        if ( commandLine.hasOption( CLIManager.VERSION ) )
        {
            version = commandLine.getOptionValue( CLIManager.VERSION );
        }
        else
        {
            showError( "You must provide your actual continuum version.", null );

            return 1;
        }

        if ( commandLine.hasOption( CLIManager.CONTINUUM_HOME ) )
        {
            continuumHome = new File( commandLine.getOptionValue( CLIManager.CONTINUUM_HOME ) );

            if ( !continuumHome.exists() || !continuumHome.isDirectory() )
            {
                showError( "You must provide a valid path for your actual continuum installation.", null );

                return 1;
            }
        }
        else
        {
            showError( "You must provide the path of your actual continuum installation.", null );

            return 1;
        }

        // ----------------------------------------------------------------------
        // 
        // ----------------------------------------------------------------------

        UpdaterManager manager = null;

        Embedder embedder = new Embedder();

        try
        {
            embedder.start( classWorld );
        }
        catch ( PlexusContainerException e )
        {
            showError( "Unable to start the embedded plexus container", e );

            return 1;
        }

        try
        {
            manager = (UpdaterManager) embedder.lookup( UpdaterManager.ROLE );
        }
        catch ( ComponentLookupException e )
        {
            showError( "Unable to configure the application", e );

            return 1;
        }

        try
        {
            manager.execute( version, continuumHome );
        }
        catch ( UpdaterException e )
        {
            showError( "Unable to upgrade your continuum.", e );

            return 1;
        }

        return 0;
    }

    private static void showError( String message, Exception e )
    {
        System.err.println( "[ERROR]: " + message );

        if ( e != null )
        {
            System.err.println( "Error stacktrace:" );

            e.printStackTrace();
        }
    }

    // ----------------------------------------------------------------------
    // Command line manager
    // ----------------------------------------------------------------------

    static class CLIManager
    {
        public static final char CONTINUUM_HOME = 'p';

        public static final char VERSION = 'v';

        public static final char HELP = 'h';

        private Options options = null;

        public CLIManager()
        {
            options = new Options();
            
            options.addOption(
                OptionBuilder.withLongOpt( "help" ).withDescription( "Display help information" ).create( HELP ) );
            
            options.addOption(
                OptionBuilder.withLongOpt( "version").hasArg().withDescription( "Actual Continuum version." ).create( VERSION ) );
            
            options.addOption(
                OptionBuilder.withLongOpt( "continuum_home").hasArg().withDescription( "Path to your Continuum." ).create( CONTINUUM_HOME ) );
        }

        public CommandLine parse( String[] args )
            throws ParseException
        {
            CommandLineParser parser = new PosixParser();

            return parser.parse( options, args );
        }

        public void displayHelp()
        {
            System.out.println();

            HelpFormatter formatter = new HelpFormatter();

            formatter.printHelp( "updater [options]", "\nOptions:", options, "\n" );
        }
    }
}