package org.apache.maven.continuum.web.view;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import com.opensymphony.webwork.ServletActionContext;
import com.opensymphony.webwork.views.util.UrlHelper;
import com.opensymphony.xwork.ActionContext;
import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.web.model.ProjectSummary;
import org.apache.maven.continuum.web.util.StateGenerator;
import org.apache.maven.continuum.security.ContinuumRoleConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.security.authorization.AuthorizationException;
import org.codehaus.plexus.security.system.SecuritySession;
import org.codehaus.plexus.security.system.SecuritySystem;
import org.codehaus.plexus.security.system.SecuritySystemConstants;
import org.codehaus.plexus.xwork.PlexusLifecycleListener;
import org.extremecomponents.table.bean.Column;
import org.extremecomponents.table.cell.DisplayCell;
import org.extremecomponents.table.core.TableModel;

import java.util.HashMap;

/**
 * Used in Summary view
 *
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 * @deprecated use of cells is discouraged due to lack of i18n and design in java code.
 *             Use jsp:include instead.
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
                    if ( isAuthorized( project ) )
                    {
                        return createActionLink( "buildResult", project, state );
                    }
                    else
                    {
                        return state;
                    }
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

        params.put( "projectGroupId", new Integer( project.getProjectGroupId() ) );

        String url = UrlHelper.buildUrl( "/" + action + ".action", ServletActionContext.getRequest(),
                                         ServletActionContext.getResponse(), params );

        return "<a href=\"" + url + "\">" + state + "</a>";
    }

    private boolean isAuthorized( ProjectSummary project )
    {
        // do the authz bit
        ActionContext context = ActionContext.getContext();

        PlexusContainer container = (PlexusContainer) context.getApplication().get( PlexusLifecycleListener.KEY );
        SecuritySession securitySession =
            (SecuritySession) context.getSession().get( SecuritySystemConstants.SECURITY_SESSION_KEY );

        try
        {
            SecuritySystem securitySystem = (SecuritySystem) container.lookup( SecuritySystem.ROLE );

            if ( !securitySystem.isAuthorized( securitySession, ContinuumRoleConstants.CONTINUUM_VIEW_GROUP_OPERATION,
                                               project.getProjectGroupName() ) )
            {
                return false;
            }
        }
        catch ( ComponentLookupException cle )
        {
            return false;
        }
        catch ( AuthorizationException ae )
        {
            return false;
        }

        return true;
    }
}
