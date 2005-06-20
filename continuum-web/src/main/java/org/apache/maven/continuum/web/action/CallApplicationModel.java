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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.action.AbstractAction;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;

import ognl.Ognl;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class CallApplicationModel
    extends AbstractAction
    implements Initializable
{
    private static String CID = "cid";

    private String applicationRole;

    private List calls;

    private Map callMap;

    public void execute( Map parameters )
        throws Exception
    {
        Object application = lookup( applicationRole );

        String cid = (String) parameters.get( CID );

        if ( cid == null )
        {
            getLogger().error( "We cannot call the application with a null CID!" );

            return;
        }

        Call call = (Call) callMap.get( cid );

        if ( call == null )
        {
            getLogger().error( "There is no call with id = " + cid + "!" );

            return;                        
        }

        String expression = call.getExpression();

        Ognl.getValue( expression, parameters, application );
    }

    // ----------------------------------------------------------------------
    // Lifecylce Management
    // ----------------------------------------------------------------------

    public void initialize()
    {
        callMap = new HashMap();

        for ( Iterator i = calls.iterator(); i.hasNext(); )
        {
            Call call = (Call) i.next();

            callMap.put( call.getCid(), call );
        }
    }
}
