package org.apache.maven.continuum.web.interceptor;

import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.interceptor.Interceptor;
import org.apache.maven.continuum.Continuum;
import org.codehaus.plexus.logging.AbstractLogEnabled;

import java.util.Map;
/*
 * Copyright 2005 The Apache Software Foundation.
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
 * UserInterceptor:
 *
 * @author Jesse McConnell <jmcconnell@apache.org>
 * @version $Id:$
 *
 * @plexus.component
 *   role="com.opensymphony.xwork.interceptor.Interceptor"
 *   role-hint="continuumInterceptor"
 */
public class ContinuumInterceptor
    extends AbstractLogEnabled
    implements Interceptor
{
    /**
     * @plexus.requirement
     */
    private Continuum continuum;


    /**
     *
     * @param actionInvocation
     * @return
     * @throws Exception
     */

    public String intercept( ActionInvocation actionInvocation )
        throws Exception
    {
        Map session  = actionInvocation.getInvocationContext().getSession();

        session.put( "continuum", continuum );

        return actionInvocation.invoke();
    }

    public void destroy()
    {
        // This space left intentionally blank
    }

    public void init()
    {
        // This space left intentionally blank
    }
}
