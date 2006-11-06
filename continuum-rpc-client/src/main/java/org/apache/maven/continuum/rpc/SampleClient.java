package org.apache.maven.continuum.rpc;

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

import org.apache.maven.continuum.model.project.Project;

import java.net.URL;

public class SampleClient
{
    public static void main( String[] args )
        throws Exception
    {
        String address = "http://localhost:8000/continuum";

        if ( args.length > 0 && args[0] != null && args[0].length() > 0 )
        {
            address = args[0];
        }

        System.out.println( "Connecting to: " + address );

        ProjectsReader pr = new ProjectsReader( new URL( address ) );

        Project[] projects = null;

        try
        {
            System.out.println( "******************************" );
            System.out.println( "Projects list" );
            System.out.println( "******************************" );

            projects = pr.readProjects();

            for ( int i = 0; i < projects.length; i++ )
            {
                System.out.println( projects[i] + " - Name=" + projects[i].getName() );
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }

        if ( projects != null && projects.length > 0 )
        {
            try
            {
                System.out.println( "******************************" );
                System.out.println( "Project detail" );
                System.out.println( "******************************" );

                Project project = new Project();
                project.setId( projects[0].getId() );
                pr.refreshProject( project );

                System.out.println( "Name for project " + project.getId() + " : " + project.getName() );

                if ( project.getState() == 1 || project.getState() == 10 )
                {
                    System.out.println( "State: New" );
                }

                if ( project.getState() == 2 )
                {
                    System.out.println( "State: OK" );
                }

                if ( project.getState() == 3 )
                {
                    System.out.println( "State: Failed" );
                }

                if ( project.getState() == 4 )
                {
                    System.out.println( "State: Error" );
                }

                if ( project.getState() == 6 )
                {
                    System.out.println( "State: Building" );
                }

                if ( project.getState() == 7 )
                {
                    System.out.println( "State: Checking out" );
                }

                if ( project.getState() == 8 )
                {
                    System.out.println( "State: Updating" );
                }

                if ( project.getState() == 9 )
                {
                    System.out.println( "State: Warning" );
                }
            }
            catch ( Exception e )
            {
                e.printStackTrace();
            }
        }
    }
}
