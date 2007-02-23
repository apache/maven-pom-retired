package org.apache.maven.continuum.web.action;

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

import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.codehaus.plexus.security.system.SecuritySession;
import org.codehaus.plexus.security.system.SecuritySystemConstants;

import java.util.Map;
import java.util.HashMap;

import com.opensymphony.xwork.ActionContext;

/**
 * Test for {@link ReleasePrepareAction}
 * 
 * @author <a href="mailto:carlos@apache.org">Carlos Sanchez</a>
 * @version $Id$
 */
public class ReleasePrepareActionTest
    extends MockObjectTestCase
{

    private ReleasePrepareAction action;

    private Mock continuumMock;

    private Mock securitySessionMock;

    private Mock actionContextMock;

    public ReleasePrepareActionTest()
    {
        action = new ReleasePrepareAction();
        continuumMock = new Mock( Continuum.class );
        //securitySessionMock = new Mock( SecuritySession.class );
        //Map map = new HashMap();
        //map.put( SecuritySystemConstants.SECURITY_SESSION_KEY, securitySessionMock );
        //action.setSession( map );
        action.setContinuum( (Continuum) continuumMock.proxy() );
    }

    /**
     * Test that the tag base url for Subversion is correctly constructed
     * 
     * @throws Exception
     */
    public void testScmTagBaseSvn()
        throws Exception
    {
        //commented out because of problems in authorization checks

        String svnUrl = "https://svn.apache.org/repos/asf/maven/continuum";
        String scmUrl = "scm:svn:" + svnUrl + "/trunk/";
        //ProjectGroup projectGroup = new ProjectGroup();
        //continuumMock.expects( once() ).method( "getProjectGroupByProjectId" ).will( returnValue( projectGroup ) );
        Project project = new Project();
        project.setScmUrl( scmUrl );
        project.setWorkingDirectory(".");
        continuumMock.expects( once() ).method( "getProject" ).will( returnValue( project ) );
        action.input();
        assertEquals( svnUrl + "/tags", action.getScmTagBase() );
        continuumMock.verify();
    }

    /**
     * Test that tag base url for non Subverson SCMs is empty
     * 
     * @throws Exception
     */
    public void testScmTagBaseNonSvn()
        throws Exception
    {
        //commented out because of problems in authorization checks

        Project project = new Project();
        project.setScmUrl( "scm:cvs:xxx" );
        project.setWorkingDirectory(".");
        continuumMock.expects( once() ).method( "getProject" ).will( returnValue( project ) );
        action.input();
        assertEquals( "", action.getScmTagBase() );
        continuumMock.verify();
    }
}
