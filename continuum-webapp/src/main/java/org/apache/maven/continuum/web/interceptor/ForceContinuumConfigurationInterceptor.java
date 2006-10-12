package org.apache.maven.continuum.web.interceptor;

import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.interceptor.Interceptor;
import org.apache.maven.continuum.Continuum;
import org.codehaus.plexus.logging.AbstractLogEnabled;
/*
 * Copyright 2006 The Apache Software Foundation.
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
 * ForceContinuumConfigurationInterceptor:
 *
 * @author: Jesse McConnell <jmcconnell@apache.org>
 * @version: $ID:$
 * @plexus.component
 *   role="com.opensymphony.xwork.interceptor.Interceptor"
 *   role-hint="forceContinuumConfigurationInterceptor"
 */
public class ForceContinuumConfigurationInterceptor
    extends AbstractLogEnabled
    implements Interceptor
{
    private static boolean checked = false;

    /**
     * @plexus.requirement
     */
    private Continuum continuum;

    public void destroy()
    {
        // no-op
    }

    public void init()
    {

    }

    public String intercept( ActionInvocation invocation )
        throws Exception
    {
        if ( checked )
        {
            return invocation.invoke();
        }

        if ( !continuum.getConfiguration().isInitialized() )
        {
            return "continuum-configuration-required";
        }

        checked = true;

        return invocation.invoke();
    }
}
