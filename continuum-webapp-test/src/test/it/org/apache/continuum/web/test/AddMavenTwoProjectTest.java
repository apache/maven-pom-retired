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

public class AddMavenTwoProjectTest
    extends AbstractAuthenticatedAdminAccessTestCase
{
    public void testAddMavenTwoProjectFromRemoteSource()
        throws Exception
    {
        // Enter values into Add Maven Two Project fields, and submit
        addMavenTwoProject( TEST_POM_URL, TEST_POM_USERNAME, TEST_POM_PASSWORD, null, true );

        clickLinkWithText( DEFAULT_PROJ_GRP_NAME );
    }

    public void testAddMavenTwoProjectFromRemoteSourceToNonDefaultProjectGroup()
        throws Exception
    {
        addProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );

        addMavenTwoProject( TEST_POM_URL, TEST_POM_USERNAME, TEST_POM_PASSWORD, TEST_PROJ_GRP_NAME, true );

        assertCellValueFromTable( TEST_PROJ_GRP_NAME, "ec_table", 2, 0 );
        assertCellValueFromTable( TEST_PROJ_GRP_ID, "ec_table", 2, 1 );
        assertCellValueFromTable( "1", "ec_table", 2, 2 );
    }

    /**
     * submit the page
     *
     * @param m2PomUrl
     * @param validPom
     */
    public void submitAddMavenTwoProjectPage( String m2PomUrl, boolean validPom )
    {
        addMavenTwoProject( m2PomUrl, "", "", null, validPom );

        if ( validPom )
        {
            assertTextPresent( "Default Project Group" );
            //TODO: Add more tests
        }
    }

    /**
     * Test invalid pom url
     */
    public void testNoPomSpecified()
    {
        submitAddMavenTwoProjectPage( "", false );
        assertTextPresent( "Either POM URL or Upload POM is required." );
    }

    /**
     * Test when scm element is missing from pom
     */
    public void testMissingScmElementPom()
    {
        String pomUrl = "http://svn.apache.org/repos/asf/maven/continuum/trunk/continuum-webapp-test/src/test/resources/unit/maven-two-projects/missing-scm-element-pom.xml";
        submitAddMavenTwoProjectPage( pomUrl, false );
        assertTextPresent( "Missing 'scm' element in the POM." );
    }

    /**
     * Test when the specified pom url is invalid
     */
    public void testCannotAccessResource()
    {
        String pomUrl = "http://svn.apache.org/asf/maven/continuum/trunk/bad_url/pom.xml";
        submitAddMavenTwoProjectPage( pomUrl, false );
        assertTextPresent(
            "POM file does not exist. Either the POM you specified or one of its modules does not exist." );
    }

    /**
     * test with a malformed pom url
     */
    public void testMalformedPomUrl()
    {
        String pomUrl = "aaa";
        submitAddMavenTwoProjectPage( pomUrl, false );
        assertTextPresent(
            "The specified resource cannot be accessed. Please try again later or contact your administrator." );
    }

    /**
     * Test when the connection element is missing from the scm tag
     */
    public void testMissingConnectionElement()
    {
        String pomUrl = "http://svn.apache.org/repos/asf/maven/continuum/trunk/continuum-webapp-test/src/test/resources/unit/maven-two-projects/missing-connection-element-pom.xml";
        submitAddMavenTwoProjectPage( pomUrl, false );
        assertTextPresent( "Missing 'connection' sub-element in the 'scm' element in the POM." );
    }

    /**
     * Test when the parent pom is missing or not yet added in continuum
     */
    public void testMissingParentPom()
    {
        String pomUrl = "http://svn.apache.org/repos/asf/maven/continuum/trunk/continuum-webapp-test/src/test/resources/unit/maven-two-projects/missing-parent-pom.xml";
        submitAddMavenTwoProjectPage( pomUrl, false );
        assertTextPresent(
            "Missing artifact trying to build the POM. Check that its parent POM is available or add it first in Continuum." );
    }

    /**
     * Test when the modules/subprojects specified in the pom are not found
     */
    public void testMissingModules()
    {
        String pomUrl= "http://svn.apache.org/repos/asf/maven/continuum/trunk/continuum-webapp-test/src/test/resources/unit/maven-two-projects/missing-modules-pom.xml";
        submitAddMavenTwoProjectPage( pomUrl, false );
        assertTextPresent( "Unknown error trying to build POM." );
    }

    /**
     * test wiht an inaccessible pom url
     */
    public void testInaccessiblePomUrl()
    {
        String pomUrl = "http://www.google.com";
        submitAddMavenTwoProjectPage( pomUrl, false );
        assertTextPresent( "The specified resource isn't a file or the protocol used isn't allowed." );
    }
}
