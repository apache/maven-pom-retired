package org.apache.maven.continuum.xmlrpc;

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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.maven.continuum.execution.ContinuumBuildExecutor;
import org.apache.maven.continuum.project.ContinuumNotifier;
import org.apache.maven.continuum.project.MavenTwoProject;
import org.apache.maven.continuum.store.ContinuumStore;

import org.codehaus.plexus.PlexusTestCase;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ContinuumXmlRpcTest
    extends PlexusTestCase
{
    public void testBasic()
        throws Exception
    {
        ContinuumXmlRpc xmlRpc = (ContinuumXmlRpc) lookup( ContinuumXmlRpc.ROLE );

        ContinuumStore store = (ContinuumStore) lookup( ContinuumStore.ROLE );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        MavenTwoProject project = makeStubMavenTwoProject( "My Project", "scm:foo" );

        project = (MavenTwoProject) store.addProject( project );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        Hashtable hashtable = xmlRpc.getProject( project.getId() );

//        dumpValue( 0, "result", hashtable );
    }

    public static MavenTwoProject makeStubMavenTwoProject( String name, String scmUrl )
    {
        return makeMavenTwoProject( name,
                                    scmUrl,
                                    "foo@bar.com",
                                    "1.0",
                                    "",
                                    ContinuumBuildExecutor.MAVEN_TWO_EXECUTOR_ID,
                                    "/tmp" );
    }

    public static MavenTwoProject makeMavenTwoProject( String name,
                                                       String scmUrl,
                                                       String emailAddress,
                                                       String version,
                                                       String commandLineArguments,
                                                       String executorId,
                                                       String workingDirectory )
    {
        MavenTwoProject project = new MavenTwoProject();

        project.setName( name );
        project.setScmUrl( scmUrl );

        List notifiers = createNotifiers( emailAddress );
        project.setNotifiers( notifiers );

        project.setVersion( version );
        project.setCommandLineArguments( commandLineArguments );
        project.setExecutorId( executorId );
        project.setWorkingDirectory( workingDirectory );

        return project;
    }

    private static List createNotifiers( String emailAddress )
    {
        ContinuumNotifier notifier = new ContinuumNotifier();

        notifier.setType( "mail" );

        Properties props = new Properties();

        props.put( "address", emailAddress );

        notifier.setConfiguration( props );

        List notifiers = new ArrayList();

        notifiers.add( notifier );

        return notifiers;
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void dumpValue( int indent, String key, Object value )
    {
        if ( value instanceof Hashtable )
        {
            dumpHashtable( indent, key, (Hashtable) value );
        }
        else if ( value instanceof Vector )
        {
            dumpVector( indent, key, (Vector) value );
        }
        else
        {
            System.err.println( makeIndent( indent ) + key + "=" + value );
        }
    }

    private void dumpHashtable( int indent, String key, Hashtable hashtable )
    {
        Map map = new TreeMap( hashtable );

        System.err.println( makeIndent( indent ) + key + " = {" );

        for ( Iterator it = map.entrySet().iterator(); it.hasNext(); )
        {
            Map.Entry entry = (Map.Entry) it.next();

            Object value = entry.getValue();

            dumpValue( indent + 1, (String) entry.getKey(), value );
        }

        System.err.println( makeIndent( indent ) + "}" );
    }

    private void dumpVector( int indent, String key, Vector vector )
    {
        System.err.println( makeIndent( indent ) + key + " = [" );

        Iterator it;

        int i;

        for ( it = vector.iterator(), i = 0; it.hasNext(); i++ )
        {
            Object value = it.next();

            dumpValue( indent + 1, "#" + i, value );
        }

        System.err.println( makeIndent( indent ) + "]" );
    }

    private String makeIndent( int size )
    {
        String s = "";

        for ( int i = 0; i < size; i++ )
        {
            s += " ";
        }

        return s;
    }
}
