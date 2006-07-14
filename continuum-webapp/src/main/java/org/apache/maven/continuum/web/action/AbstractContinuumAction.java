package org.apache.maven.continuum.web.action;

/*
 * Copyright 2001-2006 The Apache Software Foundation.
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

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import com.opensymphony.xwork.ActionSupport;

/**
 * AbstractContinuumAction:
 *
 * @author: jesse
 * @date: Jul 13, 2006
 * @version: $ID:$
 */
public abstract class AbstractContinuumAction
    extends ActionSupport
    implements LogEnabled
{
    private Logger logger;

    public void enableLogging( Logger logger )
    {
        this.logger = logger;
    }

    protected Logger getLogger()
    {
        return logger;
    }

    protected void setupLogger( Object component )
    {
        setupLogger( component, logger );
    }

    protected void setupLogger( Object component, String subCategory )
    {
        if ( subCategory == null )
        {
            throw new IllegalStateException( "Logging category must be defined." );
        }

        Logger logger = this.logger.getChildLogger( subCategory );

        setupLogger( component, logger );
    }

    protected void setupLogger( Object component, Logger logger )
    {
        if ( component instanceof LogEnabled )
        {
            ( (LogEnabled) component ).enableLogging( logger );
        }
    }

}
