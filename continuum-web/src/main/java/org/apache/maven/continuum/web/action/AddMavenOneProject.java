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

import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.codehaus.plexus.formica.Form;
import org.codehaus.plexus.formica.action.AbstractEntityAction;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;

import java.util.Map;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id$
 */
public class AddMavenOneProject
    extends AbstractEntityAction
    implements Contextualizable
{
    protected void uponSuccessfulValidation( Form form, String entityId, Map parameters )
        throws Exception
    {
        Continuum continuum = (Continuum) container.lookup( Continuum.ROLE );

        ContinuumProjectBuildingResult result = continuum.addMavenOneProject( (String) parameters.get( "m1PomUrl" ) );

        if ( result.getWarnings().size() > 0 )
        {
            setResultMessages( result.getWarnings(), parameters );
        }
    }
}
