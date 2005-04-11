package org.apache.maven.continuum.web.pipeline.valve;

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

import java.io.IOException;

import org.codehaus.plexus.summit.exception.SummitException;
import org.codehaus.plexus.summit.pipeline.valve.AbstractValve;
import org.codehaus.plexus.summit.rundata.RunData;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id: LoginValve.java,v 1.1 2005/04/04 14:05:38 jvanzyl Exp $
 */
public class LoginValve
    extends AbstractValve
{
    public void invoke( RunData data )
        throws IOException, SummitException
    {
        String skip = data.getRequest().getParameter( "skipLogin" );

        if ( skip != null && skip.equals( "true" ) )
        {
            return;
        }

        String loggedIn = (String) data.getRequest().getSession().getAttribute( "loggedIn" );

        if ( loggedIn == null )
        {
            data.setTarget( "login.form" );
        }
    }
}
