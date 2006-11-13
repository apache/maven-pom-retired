package org.apache.continuum.web.test;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: deng
 * Date: Nov 7, 2006
 * Time: 10:03:06 AM
 * To change this template use File | Settings | File Templates.
 */
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
     * Test multi module projects
     */
    public void testMultiModuleProject()
    {
        File pomFile =
            new File( getBasedir(), "src/test/resources/unit/maven-two-projects/multi-module-maven-project/pom.xml" );
        submitAddMavenTwoProjectPage( "file:/" + pomFile.getAbsolutePath(), "", true );
        assertTextPresent( "Maven Two Multi-Module Project" );
        assertTextPresent( "maven.two.multi.module.project" );

        clickLinkWithText( "Maven Two Multi-Module Project" );
        assertTextPresent( "Summary" );
        assertTextPresent( "Summary" );
        assertTextPresent( "Maven Two Multi-Module Project" );
        assertTextPresent( "Maven Two Multi-Module Project Module 1" );
        assertTextPresent( "Maven Two Multi-Module Project Module 2" );
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
     * Test when notifier(s) are specified in the pom
     */
    public void testNotifiers()
    {
        File pomFile =
            new File( getBasedir(), "src/test/resources/unit/maven-two-projects/specified-notifiers-pom.xml" );
        submitAddMavenTwoProjectPage( "file:/" + pomFile.getAbsolutePath(), "", true );
        assertTextPresent( "Maven Two Notifiers Project" );

        clickLinkWithText( "Maven Two Notifiers Project" );
        clickLinkWithText( "Notifier" );

        assertTextPresent( "Project Group Notifiers" );
        assertTextPresent( "mail" );
        assertTextPresent( "Error" );
        assertTextPresent( "Fail" );
        assertTextPresent( "Success" );
        assertTextNotPresent( "Warning" );
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

    /**
     * Test valid pom
     */
    public void testValidPom()
    {
        File pomFile = new File( getBasedir(), "src/test/resources/unit/maven-two-projects/valid-pom.xml" );
        submitAddMavenTwoProjectPage( "file:/" + pomFile.getAbsolutePath(), "", true );
        assertTextPresent( "Maven Two Project" );
        assertTextPresent( "maven.two.project" );
    }

}
