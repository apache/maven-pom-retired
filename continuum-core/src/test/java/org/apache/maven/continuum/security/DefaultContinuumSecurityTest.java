package org.apache.maven.continuum.security;

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
import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.model.system.ContinuumUser;

/**
 * @author <a href="mailto:evenisse@apache">Emmanuel Venisse</a>
 * @version $Id$
 */
public class DefaultContinuumSecurityTest
    extends AbstractContinuumTest
{
    private Continuum continuum;

    public void setUp()
        throws Exception
    {
        super.setUp();

        continuum = (Continuum) lookup( Continuum.ROLE );
    }

    public void testSecurity()
        throws Exception
    {
        ContinuumSecurity secu = continuum.getSecurity();

        ContinuumUser guest = secu.getGuestUser();

        assertEquals( "guest", guest.getUsername() );

        assertTrue( secu.isAuthorized( guest, "showProject") );

        assertFalse( secu.isAuthorized( guest, "addProject") );

        assertFalse( secu.isAuthorized( guest, "manageUsers") );
    }
}
