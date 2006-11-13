package org.apache.continuum.web.test;

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

import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.DefaultConsumer;

import java.io.File;

/**
 * Class for testing add maven one project UI page.
 */
public class AddMavenOneProjectTestCase
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
        clickLinkWithText( "Maven 1.x Project" );
    }

    /**
     * submit the page
     *
     * @param m1PomUrl
     * @param m1PomFile
     * @param validPom
     */
    public void submitAddMavenOneProjectPage( String m1PomUrl, String m1PomFile, boolean validPom )
    {
        getSelenium().type( "m1PomUrl", m1PomUrl );
        getSelenium().type( "m1PomFile", m1PomFile );

        getSelenium().click( "//input[@type='submit']" );
        waitPage();

        if( validPom )
        {
            assertPage( "Continuum - Group Summary" );
            assertTextPresent( "Project Groups" );
            //assertTextPresent( "Default Project Group" );
        }
    }

    //TODO: problem with input type="file", selenium.type(..) does not work,
    // TODO: refer to http://forums.openqa.org/thread.jspa?messageID=1365&#1365 for workaround
    /**
     * test with valid pom file
     */
   /* public void testValidPomFile()
    {
        File pomFile = new File( getBasedir(), "src/test/resources/unit/valid-maven-project/project.xml");
        submitAddMavenOneProjectPage( "", pomFile.getAbsolutePath(), false );
        assertTextPresent( "Maven One Project" );
    }*/

    /**
     * test with valid pom url
     */
    public void testValidPomUrl()
    {
        File pomFile = new File( getBasedir(), "src/test/resources/unit/maven-one-projects/valid-project.xml");
        submitAddMavenOneProjectPage( "file:/" + pomFile.getAbsolutePath(), "", true );
        assertTextPresent( "Maven One Project" );
    }

    /**
     * test with no pom file or pom url specified
     */
    public void testNoPomSpecified()
    {
        submitAddMavenOneProjectPage( "", "", false );
        assertTextPresent( "Either POM URL or Upload POM is required." );
        assertElementPresent( "m1PomUrl" );
        assertElementPresent( "m1PomFile" );
    }

    /**
     * test with missing <repository> element in the pom file
     */
    public void testMissingElementInPom()
    {
        File pomFile = new File( getBasedir(), "src/test/resources/unit/maven-one-projects/missing-repository-element-project.xml");
        submitAddMavenOneProjectPage( "file:/" + pomFile.getAbsolutePath(), "", false );
        assertTextPresent( "Missing repository element in the POM." );
    }

    
    /**
     * test with <extend> element present in pom file
     */
    public void testWithExtendElementPom()
    {
        File pomFile = new File( getBasedir(), "src/test/resources/unit/maven-one-projects/extend-element-project.xml");
        submitAddMavenOneProjectPage( "file:/" + pomFile.getAbsolutePath(), "", false );
        assertTextPresent( "Cannot use a POM with an extend element." );
    }

    /**
     * test with unparseable xml content for pom file
     */
    public void testUnparseableXmlContent()
    {
        File pomFile = new File( getBasedir(), "src/test/resources/unit/maven-one-projects/unparseable-content-project.xml");
        submitAddMavenOneProjectPage( "file:/" + pomFile.getAbsolutePath(), "", false );
        assertTextPresent( "The XML content of the POM can not be parsed." );
    }

    /**
     * test with a malformed pom url
     */
    public void testMalformedPomUrl()
    {
        File pomFile = new File( getBasedir(), "src/test/resources/unit/maven-one-projects/valid-project.xml");
        submitAddMavenOneProjectPage( pomFile.getAbsolutePath(), "", false );
        assertTextPresent( "The URL provided is malformed." );
    }

    /**
     * test wiht an inaccessible pom url
     */
    public void testInaccessiblePomUrl()
    {
        File pomFile = new File( getBasedir(), "src/test/resources/unit/maven-one-projects/valid-project.xml");
        submitAddMavenOneProjectPage( "file://" + pomFile.getAbsolutePath(), "", false );
        assertTextPresent( "The specified host is either unknown or inaccessible." );
    }

}
