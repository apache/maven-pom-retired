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

import com.opensymphony.webwork.views.util.UrlHelper;
import org.apache.maven.continuum.web.model.SummaryProjectModel;
import org.extremecomponents.table.bean.Column;
import org.extremecomponents.table.cell.DisplayCell;
import org.extremecomponents.table.core.TableModel;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import java.util.HashMap;

/**
 * Used in Summary view
 *
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class BuildCell
    extends DisplayCell
{
    protected String getCellValue( TableModel tableModel, Column column )
    {
        SummaryProjectModel project = (SummaryProjectModel) tableModel.getCurrentRowBean();

        String contextPath = tableModel.getContext().getContextPath();

        int buildNumber = project.getBuildNumber();

        String result = "<div align=\"center\">";

        if ( project.isInQueue() )
        {
            result += "<img src=\"" + contextPath + "/images/inqueue.gif\" alt=\"In Queue\" title=\"In Queue\" border=\"0\">";
        }
        else
        {
            if ( project.getState() == 1 || project.getState() == 10 || project.getState() == 2 ||
                project.getState() == 3 || project.getState() == 4 )
            {
                if ( project.getBuildNumber() > 0 )
                {
                    HashMap params = new HashMap();

                    params.put( "projectId", new Integer( project.getId() ) );

                    params.put( "projectName", project.getName() );

                    params.put( "buildId", new Integer( buildNumber ) );

                    PageContext pageContext = (PageContext) tableModel.getContext().getContextObject();

                    HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();

                    HttpServletResponse response = (HttpServletResponse) pageContext.getResponse();

                    String url = UrlHelper.buildUrl( "/buildResult.action", request, response, params );

                    result += "<a href=\"" + url + "\">" + project.getBuildNumber() + "</a>";
                }
                else
                {
                    result += "&nbsp;";
                }
            }
            else if ( project.getState() == 6 )
            {
                result += "<img src=\"" + contextPath + "/images/building.gif\" alt=\"Building\" title=\"Building\" border=\"0\">";
            }
            else if ( project.getState() == 7 )
            {
                result += "<img src=\"" + contextPath + "/images/checkingout.gif\" alt=\"Checking Out sources\" title=\"Checking Out sources\" border=\"0\">";
            }
            else if ( project.getState() == 8 )
            {
                result += "<img src=\"" + contextPath + "/images/checkingout.gif\" alt=\"Updating sources\" title=\"Updating sources\" border=\"0\">";
            }
            else
            {
                result += "<img src=\"" + contextPath + "/images/inqueue.gif\" alt=\"In Queue\" title=\"In Queue\" border=\"0\">";
            }
        }

        return result + "</div>";
    }
}
