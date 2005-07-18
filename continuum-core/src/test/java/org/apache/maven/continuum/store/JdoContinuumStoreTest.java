/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package org.apache.maven.continuum.store;

import org.apache.maven.continuum.project.ContinuumNotifier;
import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.project.MavenTwoProject;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class JdoContinuumStoreTest
    extends AbstractContinuumStoreTest
{
    public JdoContinuumStoreTest()
    {
        super( "jdo", JdoContinuumStore.class );
    }

    public void testNotifiersAreBeingDetached()
        throws Exception
    {
        List notifiers = new ArrayList();

        ContinuumNotifier notifier = new ContinuumNotifier();

        notifier.setType( "foo" );

        Map configuration = new HashMap();

        configuration.put( "moo", "foo" );

        notifier.setConfiguration( configuration );

        notifiers.add( notifier );

        ContinuumProject project = new MavenTwoProject();

        project.setNotifiers( notifiers );

        String id = getStore().addProject( project );

        project = getStore().getProject( id );

        assertNotNull( project.getNotifiers() );
    }
}
