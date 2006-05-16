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
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.codehaus.plexus.formica.web.ContentGenerator;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.summit.pull.RequestTool;
import org.codehaus.plexus.summit.rundata.RunData;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id$
 */
public class ContinuumStateContentGenerator
    extends AbstractLogEnabled
    implements ContentGenerator, RequestTool
{
    private String contextPath;

    public String generate( Object item )
    {
        int state;

        if ( item instanceof Project )
        {
            Project project = (Project) item;
            state = project.getState();
        }
        else
        {
            BuildResult buildResult = (BuildResult) item;

            state = buildResult.getState();
        }

        if ( state == ContinuumProjectState.NEW || state == ContinuumProjectState.CHECKEDOUT )
        {
            return "New";
        }
        else if ( state == ContinuumProjectState.OK )
        {
            return "<img src=\"" + contextPath +
                "/images/icon_success_sml.gif\" alt=\"Success\" title=\"Success\" border=\"0\" />";
        }
        else if ( state == ContinuumProjectState.FAILED )
        {
            return "<img src=\"" + contextPath +
                "/images/icon_warning_sml.gif\" alt=\"Failed\" title=\"Failed\" border=\"0\" />";
        }
        else if ( state == ContinuumProjectState.ERROR )
        {
            return "<img src=\"" + contextPath +
                "/images/icon_error_sml.gif\" alt=\"Error\" title=\"Error\" border=\"0\" />";
        }
        else if ( state == ContinuumProjectState.BUILDING )
        {
            return "<img src=\"" + contextPath +
                "/images/building.gif\" alt=\"Building\" title=\"Building\" border=\"0\">";
        }
        else if ( state == ContinuumProjectState.UPDATING )
        {
            return "<img src=\"" + contextPath +
                "/images/checkingout.gif\" alt=\"Checking Out sources\" title=\"Checking Out sources\" border=\"0\">";
        }
        else if ( state == ContinuumProjectState.CHECKING_OUT )
        {
            return "<img src=\"" + contextPath +
                "/images/checkingout.gif\" alt=\"Updating sources\" title=\"Updating sources\" border=\"0\">";
        }
        else
        {
            getLogger().warn( "Unknown state '" + state + "'." );

            return "Unknown";
        }
    }

    public void setRunData( RunData data )
    {
        contextPath = data.getContextPath();
    }

    public void refresh()
    {
        // empty
    }
}
