package org.apache.maven.continuum.web.view.projectview;

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

import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.extremecomponents.table.bean.Column;
import org.extremecomponents.table.cell.DisplayCell;
import org.extremecomponents.table.core.TableModel;

/**
 * Used in Project view
 *
 * @deprecated use of cells is discouraged due to lack of i18n and design in java code.
 *             Use jsp:include instead.
 *
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class NotifierEventCell
    extends DisplayCell
{
    protected String getCellValue( TableModel tableModel, Column column )
    {
        ProjectNotifier notifier = (ProjectNotifier) tableModel.getCurrentRowBean();

        String event = "";

        if ( notifier.isSendOnSuccess() )
        {
            event += "Success ";
        }

        if ( notifier.isSendOnFailure() )
        {
            event += "Failures ";
        }

        if ( notifier.isSendOnWarning() )
        {
            event += "Warnings ";
        }

        if ( notifier.isSendOnError() )
        {
            event += "Errors";
        }

        return event;
    }
}