package org.apache.maven.continuum.web.action;

import org.codehaus.plexus.xwork.action.PlexusActionSupport;

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

/**
 * TestAction:
 *
 * @author jesse
 * @version $Id$
 */
public class ActionStub
    extends PlexusActionSupport
{
    private String testString;

    public String execute()
        throws Exception
    {
        getLogger().info( testString );

        return INPUT;
    }

    public String getTestString()
    {
        return testString;
    }

    public void setTestString( String testString )
    {
        this.testString = testString;
    }
}
