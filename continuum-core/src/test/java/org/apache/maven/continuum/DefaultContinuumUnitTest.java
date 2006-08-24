package org.apache.maven.continuum;

/*
 * Copyright 2006 The Apache Software Foundation.
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

import java.util.Collections;

import org.codehaus.plexus.action.Action;
import org.codehaus.plexus.action.ActionManager;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

/**
 * Test for {@link DefaultContinuum}.
 *
 * @author <a href="mailto:carlos@apache.org">Carlos Sanchez</a>
 * @version $Id$
 */
public class DefaultContinuumUnitTest
    extends MockObjectTestCase
{

    public void testExecuteAction()
        throws Exception
    {
        DefaultContinuum continuum = new DefaultContinuum();

        Mock actionMock = new Mock( Action.class );

        Mock actionManagerMock = new Mock( ActionManager.class );

        actionManagerMock.expects( once() ).will( returnValue( actionMock.proxy() ) );

        continuum.setActionManager( (ActionManager) actionManagerMock.proxy() );

        actionMock.expects( once() ).will( throwException( new ContinuumException( "" ) ) );

        String exceptionName = ContinuumException.class.getName();
        try
        {
            continuum.executeAction( "", Collections.EMPTY_MAP );
            fail( exceptionName + " must have been thrown" );
        }
        catch ( ContinuumException e )
        {
            //expected, check for twice wrapped exception
            if ( e.getCause() != null )
            {
                assertFalse( exceptionName + " is wrapped in " + exceptionName, e.getCause().getClass()
                    .equals( ContinuumException.class ) );
            }
        }
    }
}
