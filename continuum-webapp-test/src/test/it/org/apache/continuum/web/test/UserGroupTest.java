package org.apache.continuum.web.test;

/*
 * Copyright 2005-2006 The Apache Software Foundation.
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
 * Test case for User group.
 */
public class UserGroupTest
    extends AbstractAuthenticatedAdminAccessTestCase
{
    public void testUserGroup()
        throws Exception
    {
        goToUserGroup();
    }

    /*public void testAddAntProject()
    {
        goToAddAntPage();
        setFieldValue( "projectName", "Foo" );
        setFieldValue( "projectVersion", "1.0-SNAPSHOT" );
        setFieldValue( "projectScmUrl",
                            "https://svn.apache.org/repos/asf/maven/continuum/trunk/continuum-test-projects/ant/" );
        clickButtonWithValue( "Add" );
        assertGroupSummaryPage();
        assertTextPresent( "Default Project Group" );
        clickLinkWithText( "Default Project Group");
        assertTextPresent( "Foo");

        //TODO Add more tests (values in Default Project Group, values in project view, notifiers, build defintions, delete, build,...)
    }*/

    public void goToUserGroup()
        throws Exception
    {
        clickLinkWithText( "Users" );
        assertUsersListPage();
    }

}
