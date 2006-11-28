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

import java.io.File;

//TODO: Remove this class and move all tests in AddMavenTwoProjectTest
//tests need probably to be refactored
public class AddMavenTwoProjectTestCase
    extends AbstractAuthenticatedAccessTestCase
{
    public String getUsername()
    {
        return adminUsername;
    }

    public String getPassword()
    {
        return adminPassword;
    }

    public void setUp()
        throws Exception
    {
        super.setUp();
        clickLinkWithText( "Maven 2.0.x Project" );
    }

    /**
     * submit the page
     *
     * @param m2PomUrl
     * @param m2PomFile
     * @param validPom
     */
    public void submitAddMavenTwoProjectPage( String m2PomUrl, String m2PomFile, boolean validPom )
    {
        getSelenium().type( "m2PomUrl", m2PomUrl );
        getSelenium().type( "m2PomFile", m2PomFile );

        getSelenium().click( "//input[@type='submit']" );
        waitPage();

        if ( validPom )
        {
            assertPage( "Continuum - Group Summary" );
            assertTextPresent( "Project Groups" );
            assertTextPresent( "Default Project Group" );
        }
    }

    /**
     * Test invalid pom url
     */
    public void testNoPomSpecified()
    {
        submitAddMavenTwoProjectPage( "", "", false );
        assertTextPresent( "Either POM URL or Upload POM is required." );
        assertElementPresent( "m2PomUrl" );
        assertElementPresent( "m2PomFile" );
    }

    /**
     * Test when scm element is missing from pom
     */
    public void testMissingScmElementPom()
    {
        File pomFile =
            new File( getBasedir(), "src/test/resources/unit/maven-two-projects/missing-scm-element-pom.xml" );
        submitAddMavenTwoProjectPage( "file:/" + pomFile.getAbsolutePath(), "", false );
        assertTextPresent( "Missing scm element in the POM." );
        assertElementPresent( "m2PomUrl" );
        assertElementPresent( "m2PomFile" );
    }

    /**
     * Test when the specified pom url is invalid
     */
    public void testCannotAccessResource()
    {
        File pomFile = new File( getBasedir(), "src/test/resources/unit/maven-two-projects/valid-pom.xml" );
        submitAddMavenTwoProjectPage( "file://" + pomFile.getAbsolutePath(), "", false );
        assertTextPresent(
            "The specified resource cannot be accessed. Please try again later or contact your administrator." );
        assertElementPresent( "m2PomUrl" );
        assertElementPresent( "m2PomFile" );
    }

    /**
     * Test when the connection element is missing from the scm tag
     */
    public void testMissingConnectionElement()
    {
        File pomFile =
            new File( getBasedir(), "src/test/resources/unit/maven-two-projects/missing-connection-element-pom.xml" );
        submitAddMavenTwoProjectPage( "file:/" + pomFile.getAbsolutePath(), "", false );
        assertTextPresent( "Missing connection sub-element in the scm element in the POM." );
        assertElementPresent( "m2PomUrl" );
        assertElementPresent( "m2PomFile" );
    }

    /**
     * Test when the parent pom is missing or not yet added in continuum
     */
    public void testMissingParentPom()
    {
        File pomFile = new File( getBasedir(), "src/test/resources/unit/maven-two-projects/missing-parent-pom.xml" );
        submitAddMavenTwoProjectPage( "file:/" + pomFile.getAbsolutePath(), "", false );
        assertTextPresent(
            "Missing artifact trying to build the POM. Check that its parent POM is available or add it first in Continuum." );
        assertElementPresent( "m2PomUrl" );
        assertElementPresent( "m2PomFile" );
    }

    /**
     * Test when the modules/subprojects specified in the pom are not found
     */
    public void testMissingModules()
    {
        File pomFile = new File( getBasedir(), "src/test/resources/unit/maven-two-projects/missing-modules-pom.xml" );
        submitAddMavenTwoProjectPage( "file:/" + pomFile.getAbsolutePath(), "", false );
        assertTextPresent( "Unknown error trying to build POM." );
        assertElementPresent( "m2PomUrl" );
        assertElementPresent( "m2PomFile" );
    }
}
