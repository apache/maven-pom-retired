package org.apache.maven.continuum.web.filter;

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

import javax.servlet.Filter;

/**
 * <p>Delegates <code>Filter</code> requests to a Plexus component.</p>
 * 
 * <p>This class acts as a proxy on behalf of a
 * target {@link Filter} that is instantiated by the Plexus container. It is necessary to specify which target
 * {@link Filter} should be proxied as a filter initialization parameter.</p>
 * 
 * <p>To use this filter, it is necessary to specify the following filter initialization parameter:
 *  <ul>
 *      <li><code>component</code> indicates the name of the target <code>Filter</code> defined in the container.
 *      The only requirements are that this component implements the <code>javax.servlet.Filter</code>
 *      interface and is available in the <code>Container</code> under that name.</li>
 *  </ul>
 * </p>
 * 
 * <p>A final optional initialization parameter, <code>lifecycle</code>, determines whether the servlet container
 * or the IoC container manages the lifecycle of the proxied filter. When possible you should write your filters to be
 * managed via the IoC container interfaces. If you cannot control the filters you wish to proxy (eg
 * you do not have their source code) you might need to allow the servlet container to manage lifecycle via the {@link
 * javax.servlet.Filter#init(javax.servlet.FilterConfig)} and {@link javax.servlet.Filter#destroy()} methods. If this
 * case, set the <code>lifecycle</code> initialization parameter to <code>servlet-container-managed</code>. If the
 * parameter is any other value, servlet container lifecycle methods will not be delegated through to the proxy.</p>
 * 
 * @deprecated use {@link org.codehaus.plexus.xwork.filter.FilterToComponentProxy}
 * 
 * @author Ben Alex
 * @author Emmanuel Venisse (evenisse at apache dot org)
 * @author <a href="mailto:carlos@apache.org">Carlos Sanchez</a>
 * @version $Id$
 */
public class FilterToComponentProxy
    extends org.codehaus.plexus.xwork.filter.FilterToComponentProxy
{
}
