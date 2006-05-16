package org.apache.maven.continuum.web.action;

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

import org.apache.maven.continuum.model.system.ContinuumUser;
import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.web.model.SessionUser;
import org.codehaus.plexus.security.summit.SecureRunData;
import org.codehaus.plexus.action.AbstractAction;

import java.util.Map;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class Logout
    extends AbstractAction
{
    /**
     * @plexus.requirement
     */
    private ContinuumStore store;

    public void execute( Map map )
        throws Exception
    {
        SecureRunData secData = (SecureRunData) map.get( "data" );

        secData.setUser( null );

        secData.setTarget( "Summary.vm" );
    }
}
