package org.apache.maven.continuum.web.tool;

import org.codehaus.plexus.summit.pull.RequestTool;
import org.codehaus.plexus.summit.rundata.RunData;

import javax.servlet.http.HttpServletRequest;

/*
 * Copyright 2004-2006 The Apache Software Foundation.
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
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class RequestUtil
    implements RequestTool
{
    private String contextPath;

    private HttpServletRequest request;

    public String getParameter( String paramName )
    {
        return getRequest().getParameter( paramName );
    }

    public HttpServletRequest getRequest()
    {
        return request;
    }

    public String getContextPath()
    {
        return contextPath;
    }

    public void setRunData( RunData data )
    {
        contextPath = data.getContextPath();

        request = data.getRequest();
    }

    public void refresh()
    {
        // empty
    }
}
