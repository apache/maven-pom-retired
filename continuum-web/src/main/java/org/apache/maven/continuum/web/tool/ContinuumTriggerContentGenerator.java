package org.apache.maven.continuum.web.tool;

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

import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.codehaus.plexus.formica.web.ContentGenerator;
import org.codehaus.plexus.logging.AbstractLogEnabled;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id$
 */
public class ContinuumTriggerContentGenerator
    extends AbstractLogEnabled
    implements ContentGenerator
{

    public String generate( Object item )
    {
        int trigger;

        BuildResult result = (BuildResult) item;
        trigger = result.getTrigger();

        if ( trigger == ContinuumProjectState.TRIGGER_UNKNOWN )
        {
            return "Scheduled";
        }
        else if ( trigger == ContinuumProjectState.TRIGGER_FORCED )
        {
            return "Forced";
        }
        else
        {
            getLogger().warn( "Unknown trigger '" + trigger + "'." );

            return "Unknown";
        }
    }
}
