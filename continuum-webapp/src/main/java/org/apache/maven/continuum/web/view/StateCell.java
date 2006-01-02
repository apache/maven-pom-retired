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

import org.apache.maven.continuum.web.model.SummaryProjectModel;
import org.apache.maven.continuum.web.util.StateGenerator;

import org.extremecomponents.table.bean.Column;
import org.extremecomponents.table.cell.DisplayCell;
import org.extremecomponents.table.core.BaseModel;

import com.opensymphony.webwork.ServletActionContext;
import com.opensymphony.webwork.views.util.UrlHelper;

import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;

/**
 * Used in Summary view
 *
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class StateCell
    extends DisplayCell
{
    public void init(BaseModel model, Column column)
    {
        super.init(model, column);

        SummaryProjectModel project = (SummaryProjectModel) model.getCurrentCollectionBean();

        int latestBuildId = project.getLatestBuildId();

        HttpServletRequest request = (HttpServletRequest) model.getPageContext().getRequest();

        String state = StateGenerator.generate( project.getState(), request.getContextPath() );

        if ( project.getState() == 1 || project.getState() == 2 || project.getState() == 3 || project.getState() == 4 )
        {
            if ( latestBuildId != -1 && !StateGenerator.NEW.equals( state ) )
            {
                HashMap params = new HashMap();

                params.put( "projectId", new Integer( project.getId() ) );

                params.put( "projectName", project.getName() );

                params.put( "buildId", new Integer( latestBuildId ) );

                String url = UrlHelper.buildUrl( "/buildResult.action", ServletActionContext.getRequest(),
                                                 ServletActionContext.getResponse(), params );

                column.setValue( "<a href=\"" + url + "\">" + state + "</a>" );
            }
            else
            {
                column.setValue( state );
            }
        }
        else
        {
            column.setValue( "&nbsp;" );
        }
    }
}
