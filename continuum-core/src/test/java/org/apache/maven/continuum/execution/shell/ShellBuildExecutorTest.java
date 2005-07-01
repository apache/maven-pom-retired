package org.apache.maven.continuum.execution.shell;

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
import org.apache.maven.continuum.execution.ContinuumBuildExecutorException;
import org.apache.maven.continuum.project.ShellProject;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ShellBuildExecutorTest
    extends AbstractContinuumTest
{
    public void testNonAbsolutePath()
        throws Exception
    {
        ShellBuildExecutor buildExecutor = getShellBuildExecutor();

        ShellProject project = makeStubShellProject( "Shell project", "scm:foo" );

        String script = project.getExecutable();

        buildExecutor.updateProjectFromCheckOut( null, project );

        assertEquals( script, project.getExecutable() );
    }

    public void testAbsolutePath()
        throws Exception
    {
        ShellProject project = makeStubShellProject( "Shell project", "scm:foo" );

        project.setExecutable( getTestFile( "foo.sh" ).getAbsolutePath() );

        try
        {
            getShellBuildExecutor().updateProjectFromCheckOut( null, project );

            fail( "Expected ContinuumBuildExecutorException" );
        }
        catch ( ContinuumBuildExecutorException e )
        {
            // expected
        }
    }
}
