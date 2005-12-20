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

import javax.servlet.http.HttpServletRequest;

/**
 * Used in Summary view
 *
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class BuildCell
    extends DisplayCell
{
    public void init(BaseModel model, Column column)
    {
        super.init(model, column);

        SummaryProjectModel project = (SummaryProjectModel) model.getCurrentCollectionBean();

        int buildNumber = project.getBuildNumber();

        if ( project.isInQueue() )
        {
            column.setValue( "In&nbsp;queue" );
        }
        else if ( project.getState() == 1 || project.getState() == 2 || project.getState() == 3 || project.getState() == 4 )
        {
            if ( project.getBuildNumber() > 0 )
            {
                column.setValue( "<a href=\"TO_BE_DEFINE\">" + project.getBuildNumber() + "</a>" );
            }
            else
            {
                column.setValue( "&nbsp;" );
            }
        }
        else
        {
            column.setValue( "In&nbsp;progress" );
        }
    }
}
