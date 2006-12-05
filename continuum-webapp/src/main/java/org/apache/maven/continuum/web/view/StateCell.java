package org.apache.maven.continuum.web.view;

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

import com.opensymphony.webwork.ServletActionContext;
import com.opensymphony.webwork.views.util.UrlHelper;

import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.web.model.ProjectSummary;
import org.apache.maven.continuum.web.util.StateGenerator;
import org.extremecomponents.table.bean.Column;
import org.extremecomponents.table.cell.DisplayCell;
import org.extremecomponents.table.core.TableModel;

import java.util.HashMap;

/**
 * Used in Summary view
 *
 * @deprecated use of cells is discouraged due to lack of i18n and design in java code.
 *             Use jsp:include instead.
 *
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class StateCell
    extends DisplayCell
{
    protected String getCellValue( TableModel tableModel, Column column )
    {
        ProjectSummary project = (ProjectSummary) tableModel.getCurrentRowBean();

        switch ( project.getState() )
        {
            case ContinuumProjectState.NEW:
            case ContinuumProjectState.OK:
            case ContinuumProjectState.FAILED:
            case ContinuumProjectState.ERROR:
            case ContinuumProjectState.BUILDING:
            {
                String state = StateGenerator.generate( project.getState(), tableModel.getContext().getContextPath() );

                if ( project.getLatestBuildId() != -1 && !StateGenerator.NEW.equals( state ) )
                {
                    return createActionLink( "buildResult", project, state );
                }
                else
                {
                    return state;
                }
            }

            default:
            {
                return "&nbsp;";
            }
        }
    }

    private static String createActionLink( String action, ProjectSummary project, String state )
    {
        HashMap params = new HashMap();

        params.put( "projectId", new Integer( project.getId() ) );

        params.put( "projectName", project.getName() );

        params.put( "buildId", new Integer( project.getLatestBuildId() ) );

        params.put( "projectGroupId", new Integer( project.getProjectGroupId()));

        String url = UrlHelper.buildUrl( "/" + action + ".action",
                                         ServletActionContext.getRequest(),
                                         ServletActionContext.getResponse(),
                                         params );

        return "<a href=\"" + url + "\">" + state + "</a>";
    }
}
