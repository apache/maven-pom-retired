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

import org.apache.maven.continuum.AbstractContinuumTest;
import org.apache.maven.continuum.project.MavenTwoProject;
import org.apache.maven.continuum.store.ContinuumStore;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ContinuumXmlRpcTest
    extends AbstractContinuumTest
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

        dumpValue( 0, "result", hashtable );
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
            out( makeIndent( indent ) + key + "=" + value );
        }
    }

    private void dumpHashtable( int indent, String key, Hashtable hashtable )
    {
        Map map = new TreeMap( hashtable );

        out( makeIndent( indent ) + key + " = {" );

        for ( Iterator it = map.entrySet().iterator(); it.hasNext(); )
        {
            Map.Entry entry = (Map.Entry) it.next();

            Object value = entry.getValue();

            dumpValue( indent + 1, (String) entry.getKey(), value );
        }

        out( makeIndent( indent ) + "}" );
    }

    private void dumpVector( int indent, String key, Vector vector )
    {
        out( makeIndent( indent ) + key + " = [" );

        Iterator it;

        int i;

        for ( it = vector.iterator(), i = 0; it.hasNext(); i++ )
        {
            Object value = it.next();

            dumpValue( indent + 1, "#" + i, value );
        }

        out( makeIndent( indent ) + "]" );
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

    private void out( String message )
    {
//        System.out.println( message );
    }
}
