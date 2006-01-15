package org.apache.maven.continuum.web.view.projectview;

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

import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.extremecomponents.table.bean.Column;
import org.extremecomponents.table.cell.DisplayCell;
import org.extremecomponents.table.core.BaseModel;

/**
 * Used in Project view
 *
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class NotifierRecipientCell
    extends DisplayCell
{
    public void init( BaseModel model, Column column )
    {
        super.init( model, column );

        ProjectNotifier notifier = (ProjectNotifier) model.getCurrentCollectionBean();

        if ( "irc".equals( notifier.getType() ) )
        {
            String address = "";

            if ( notifier.getConfiguration().get( "host" ) != null )
            {
                address += notifier.getConfiguration().get( "host" ) + ":";
            }

            if ( notifier.getConfiguration().get( "port" ) != null )
            {
                address += notifier.getConfiguration().get( "port" ) + ":";
            }

            if ( notifier.getConfiguration().get( "channel" ) != null )
            {
                address += notifier.getConfiguration().get( "channel" );
            }

            column.setValue( address );
        }
        else
        {
            column.setValue( notifier.getConfiguration().get( "address" ) );
        }
    }
}
