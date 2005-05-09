package org.apache.maven.continuum.project.state;

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

import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.project.ContinuumProjectState;

import org.codehaus.plexus.logging.AbstractLogEnabled;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id:$
 */
public class DefaultContinuumProjectStateGuard
    extends AbstractLogEnabled
    implements ContinuumProjectStateGuard
{
    // ----------------------------------------------------------------------
    // ContinuumProjectStateGuard Implementation
    // ----------------------------------------------------------------------

    private final static int[] NEW_PREVIOUS_STATES = {
        ContinuumProjectState.CHECKING_OUT,
    };

    private final static int[] ERROR_PREVIOUS_STATES = {
        ContinuumProjectState.CHECKING_OUT,
    };

//    private final static int[] BUILD_SIGNALED_PREVIOUS_STATES = {
//        ContinuumProjectState.NEW,
//        ContinuumProjectState.ERROR,
//        ContinuumProjectState.OK,
//        ContinuumProjectState.FAILED,
//        ContinuumProjectState.BUILD_SIGNALED,
//    };

    private final static int[] UPDATING_PREVIOUS_STATES = {
//        ContinuumProjectState.BUILD_SIGNALED,
        ContinuumProjectState.NEW,
        ContinuumProjectState.ERROR,
        ContinuumProjectState.OK,
        ContinuumProjectState.FAILED,
    };

    private final static int[] BUILDING_PREVIOUS_STATES = {
        ContinuumProjectState.UPDATING,
    };

    private final static int[] OK_PREVIOUS_STATES = {
        ContinuumProjectState.BUILDING,
    };

    private final static int[] FAILED_PREVIOUS_STATES = {
        ContinuumProjectState.BUILDING,
    };

    private final static int[] DELETABLE_PREVIOUS_STATES = {
        ContinuumProjectState.NEW,
        ContinuumProjectState.ERROR,
        ContinuumProjectState.OK,
        ContinuumProjectState.FAILED,
    };

    private static final int[] UPDATABLE_PREVIOUS_STATES = {
        ContinuumProjectState.NEW,
        ContinuumProjectState.ERROR,
        ContinuumProjectState.OK,
        ContinuumProjectState.FAILED,
        ContinuumProjectState.BUILDING,
        ContinuumProjectState.CHECKING_OUT,
    };

    private static final int[] CHANGE_WORKING_DIRECTORY_PREVIOUS_STATES = {
        ContinuumProjectState.CHECKING_OUT,
    };

    // ----------------------------------------------------------------------
    // ContinuumProjectStateGuard Implementation
    // ----------------------------------------------------------------------

    public void assertInState( ContinuumProject project, int state )
        throws ContinuumProjectStateGuardException
    {
        if ( project.getState() == state )
        {
            return;
        }

        throw new ContinuumProjectStateGuardException( "Expected project to be in state '" + decodeState( state ) + "', " +
                                                       "but it was in the state '" + decodeState( project.getState() ) + "'." );
    }

    public void assertTransition( ContinuumProject project, int newState )
        throws ContinuumProjectStateGuardException
    {
        if ( newState == ContinuumProjectState.NEW )
        {
            assertInStates( project.getState(), NEW_PREVIOUS_STATES, "new" );
        }
        else if ( newState == ContinuumProjectState.ERROR )
        {
            assertInStates( project.getState(), ERROR_PREVIOUS_STATES, "error" );
        }
        else if ( newState == ContinuumProjectState.UPDATING )
        {
            assertInStates( project.getState(), UPDATING_PREVIOUS_STATES, "updating" );
        }
//        else if ( newState == ContinuumProjectState.BUILD_SIGNALED )
//        {
//            assertInStates( project.getState(), BUILD_SIGNALED_PREVIOUS_STATES, "build signaled" );
//        }
        else if ( newState == ContinuumProjectState.BUILDING )
        {
            assertInStates( project.getState(), BUILDING_PREVIOUS_STATES, "building" );
        }
        else if ( newState == ContinuumProjectState.OK )
        {
            assertInStates( project.getState(), OK_PREVIOUS_STATES, "ok" );
        }
        else if ( newState == ContinuumProjectState.FAILED )
        {
            assertInStates( project.getState(), FAILED_PREVIOUS_STATES, "failed" );
        }
        else
        {
            throw new ContinuumProjectStateGuardException( "Unknown state '" + newState + "'." );
        }
    }

    public void assertDeletable( ContinuumProject project )
        throws ContinuumProjectStateGuardException
    {
        int[] expectedStates = DELETABLE_PREVIOUS_STATES;

        int actualState = project.getState();

        if ( isInState( expectedStates, actualState ) )
        {
            return;
        }

        String stateString = makeStateString( expectedStates );

        throw new ContinuumProjectStateGuardException(
            "To be able to delete a project the project as to be in one of the states in " + stateString + " " +
            "but the project was in the '" + decodeState( actualState ) + "' state." );
    }

    public void assertUpdatable( ContinuumProject project )
        throws ContinuumProjectStateGuardException
    {
        int[] expectedStates = UPDATABLE_PREVIOUS_STATES;

        int actualState = project.getState();

        if ( isInState( expectedStates, actualState ) )
        {
            return;
        }

        String stateString = makeStateString( expectedStates );

        throw new ContinuumProjectStateGuardException(
            "To be able to update a project the project as to be in one of the states in " + stateString + " " +
            "but the project was in the '" + decodeState( actualState ) + "' state." );
    }

    public void assertCanChangeWorkingDirectory( ContinuumProject project )
        throws ContinuumProjectStateGuardException
    {
        int[] expectedStates = CHANGE_WORKING_DIRECTORY_PREVIOUS_STATES;

        int actualState = project.getState();

        if ( isInState( expectedStates, actualState ) )
        {
            return;
        }

        String stateString = makeStateString( expectedStates );

        throw new ContinuumProjectStateGuardException(
            "To be able to change the working directory of a project the " +
            "project as to be in one of the states in " + stateString + " " +
            "but the project was in the '" + decodeState( actualState ) + "' state." );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private static void assertInStates( int actualState,
                                        int[] expectedStates,
                                        String actionName )
        throws ContinuumProjectStateGuardException
    {
        if ( isInState( expectedStates, actualState ) )
            return;

        String stateString = makeStateString( expectedStates );

        throw new ContinuumProjectStateGuardException(
            "To be able to go into the state '" + actionName + "', " +
            "the project as to be in one of the states in " + stateString + " " +
            "but the project was in the '" + decodeState( actualState ) + "' state." );
    }

    private static boolean isInState( int[] expectedStates,
                                      int actualState )
    {
        for ( int i = 0; i < expectedStates.length; i++ )
        {
            int expectedState = expectedStates[ i ];

            if ( actualState == expectedState )
            {
                return true;
            }
        }

        return false;
    }

    private static String makeStateString( int[] states )
    {
        String stateString = "[";

        for ( int i = 0; i < states.length; i++ )
        {
            int expectedState = states[ i ];

            if ( i > 0 )
            {
                stateString += ", ";
            }

            stateString += "'" + decodeState( expectedState ) + "'";
        }

        stateString += "]";

        return stateString;
    }

    // TODO: Externalize
    private static String decodeState( int state )
    {
        switch ( state )
        {
            case ContinuumProjectState.NEW:
                return "new";
            case ContinuumProjectState.OK:
                return "ok";
            case ContinuumProjectState.FAILED:
                return "failed";
            case ContinuumProjectState.ERROR:
                return "error";
//            case ContinuumProjectState.BUILD_SIGNALED:
//                return "build signaled";
            case ContinuumProjectState.BUILDING:
                return "building";
            case ContinuumProjectState.CHECKING_OUT:
                return "checking out";
            case ContinuumProjectState.UPDATING:
                return "updating";
            default:
                return "UNKNOWN (id '" + state + "')";
        }
    }
}
