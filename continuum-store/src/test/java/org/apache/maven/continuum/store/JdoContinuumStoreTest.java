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
}
