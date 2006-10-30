package org.apache.maven.continuum.web.action.notifier;

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

import java.util.HashMap;
import java.util.Map;

import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectNotifier;

/**
 * Action that deletes a {@link ProjectNotifier} of type 'Mail' from the 
 * specified {@link Project}.
 * 
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id: MailNotifierEditAction.java 465060 2006-10-17 21:24:38Z jmcconnell $
 * @since 1.1
 * 
 * @plexus.component
 *   role="com.opensymphony.xwork.Action"
 *   role-hint="mailProjectNotifierEdit"
 */
public class MailProjectNotifierEditAction
    extends AbstractProjectNotifierEditAction
{
    private String address;

    protected void initConfiguration( Map configuration )
    {
        address = (String) configuration.get( "address" );
    }

    protected void setNotifierConfiguration( ProjectNotifier notifier )
    {
        HashMap configuration = new HashMap();

        configuration.put( "address", address );

        notifier.setConfiguration( configuration );
    }

    public String getAddress()
    {
        return address;
    }

    public void setAddress( String address )
    {
        this.address = address;
    }
}
