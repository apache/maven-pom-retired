package org.apache.continuum.web.test;

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

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class AntTest
    extends AbstractAuthenticatedAdminAccessTestCase
{
    public void testAddAntProject()
        throws Exception
    {
        goToAddAntPage();
        setFieldValue( "projectName", "Foo" );
        setFieldValue( "projectVersion", "1.0-SNAPSHOT" );
        setFieldValue( "projectScmUrl",
                       "https://svn.apache.org/repos/asf/maven/continuum/trunk/continuum-test-projects/ant/" );
        clickButtonWithValue( "Add" );

        assertProjectGroupsSummaryPage();
        assertTextPresent( "Default Project Group" );
        clickLinkWithText( "Default Project Group" );
        assertTextPresent( "Foo" );

        //TODO Add more tests (values in Default Project Group, values in project view, notifiers, build defintions, delete, build,...)
    }

    public void testSubmitEmptyForm()
    {
        goToAddAntPage();
        clickButtonWithValue( "Add", false );
        assertAddAntProjectPage();
        assertTextPresent( "Name is required" );
        assertTextPresent( "Version is required" );
        assertTextPresent( "SCM Url is required" );
    }

    public void testSubmitEmptyProjectName()
    {
        goToAddAntPage();
        clickButtonWithValue( "Add", false );
        assertAddAntProjectPage();
        assertTextPresent( "Name is required" );
    }

    public void testSubmitEmptyVersion()
    {
        goToAddAntPage();
        clickButtonWithValue( "Add", false );
        assertAddAntProjectPage();
        assertTextPresent( "Version is required" );
    }

    public void testSubmitEmptyScmUrl()
    {
        goToAddAntPage();
        clickButtonWithValue( "Add", false );
        assertAddAntProjectPage();
        assertTextPresent( "SCM Url is required" );
    }

    private void goToAddAntPage()
    {
        clickLinkWithText( "Ant Project" );
        assertAddAntProjectPage();
    }

}
