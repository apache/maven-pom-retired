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

import org.apache.maven.shared.web.test.AbstractSeleniumTestCase;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public abstract class AbstractContinuumTestCase
    extends AbstractSeleniumTestCase
{
    protected String adminUsername = "admin";

    protected String adminPassword = "admin1";

    protected String adminFullName = "Continuum Admin";

    protected String adminEmail = "admin@localhost.localdomain.com";

    private String baseUrl = "http://localhost:9595/continuum";

    /**
     * We create an admin user if it doesn't exist
     */
    protected void initialize()
    {
        open( "/continuum" );
        waitPage();

        if ( "Create Admin User".equals( getTitle() ) )
        {
            assertCreateAdminUserPage();
            submitCreateAdminUserPage( adminFullName, adminEmail, adminPassword, adminPassword );
            assertLoginPage();
            submitLoginPage( adminUsername, adminPassword );
            assertEditConfigurationPage();
            submitConfigurationPage( baseUrl, null, null, null );
            logout();
        }
    }

    protected String getApplicationName()
    {
        return "Continuum";
    }

    protected String getInceptionYear()
    {
        return "2005";
    }

    public void assertHeader()
    {
        //TODO
    }

    public String getBaseUrl()
    {
        return "http://localhost:9595/continuum";
    }

    //////////////////////////////////////
    // Create Admin User
    //////////////////////////////////////
    public void assertCreateAdminUserPage()
    {
        assertPage( "Create Admin User" );
        assertTextPresent( "Create Admin User" );
        assertTextPresent( "Username" );
        assertElementPresent( "user.username" );
        assertTextPresent( "Full Name" );
        assertElementPresent( "user.fullName" );
        assertTextPresent( "Email Address" );
        assertElementPresent( "user.email" );
        assertTextPresent( "Password" );
        assertElementPresent( "user.password" );
        assertTextPresent( "Confirm Password" );
        assertElementPresent( "user.confirmPassword" );
    }

    public void submitCreateAdminUserPage( String fullName, String email, String password, String confirmPassword )
    {
        setFieldValue( "user.fullName", fullName );
        setFieldValue( "user.email", email );
        setFieldValue( "user.password", password );
        setFieldValue( "user.confirmPassword", confirmPassword );
        submit();
        waitPage();
    }

    //////////////////////////////////////
    // Configuration
    //////////////////////////////////////
    public void assertEditConfigurationPage()
    {
        assertPage( "Continuum - Configuration" );
        assertTextPresent( "Guest" );
        assertElementPresent( "guestAccountEnabled" );
        assertTextPresent( "Working Directory" );
        assertElementPresent( "workingDirectory" );
        assertTextPresent( "Build Output Directory" );
        assertElementPresent( "buildOutputDirectory" );
        assertTextPresent( "Deployment Repository Directory" );
        assertElementPresent( "deploymentRepositoryDirectory" );
        assertTextPresent( "Base URL" );
        assertElementPresent( "baseUrl" );
        assertTextPresent( "Company Name" );
        assertElementPresent( "companyName" );
        assertTextPresent( "Company Logo" );
        assertElementPresent( "companyLogo" );
        assertTextPresent( "Company URL" );
        assertElementPresent( "companyUrl" );
    }

    public void submitConfigurationPage( String baseUrl, String companyName, String companyLogo, String companyUrl )
    {
        setFieldValue( "baseUrl", baseUrl );
        if ( companyName != null )
        {
            setFieldValue( "companyName", companyName );
        }
        if ( companyLogo != null )
        {
            setFieldValue( "companyLogo", companyLogo );
        }
        if ( companyUrl != null )
        {
            setFieldValue( "companyUrl", companyUrl );
        }
        submit();
        waitPage();
    }

    //////////////////////////////////////
    // ANT/SHELL Projects
    //////////////////////////////////////
    public void assertAddProjectPage( String type )
    {
        String title = type.substring( 0, 1 ).toUpperCase() + type.substring( 1 ).toLowerCase();
        assertPage( "Continuum - Add " + title + " Project" );
        assertTextPresent( "Add " + title + " Project" );
        assertTextPresent( "Project Name" );
        assertElementPresent( "projectName" );
        assertTextPresent( "Version" );
        assertElementPresent( "projectVersion" );
        assertTextPresent( "Scm Url" );
        assertElementPresent( "projectScmUrl" );
        assertTextPresent( "Scm Username" );
        assertElementPresent( "projectScmUsername" );
        assertTextPresent( "Scm Password" );
        assertElementPresent( "projectScmPassword" );
        assertTextPresent( "Scm Branch/Tag" );
        assertElementPresent( "projectScmTag" );
        assertLinkPresent( "Maven SCM URL" );
    }

    public void assertAddAntProjectPage()
    {
        assertAddProjectPage( "ant" );
    }

    public void assertAddShellProjectPage()
    {
        assertAddProjectPage( "shell" );
    }

    //////////////////////////////////////
    // Group Summary
    //////////////////////////////////////
    public void assertGroupSummaryPage()
    {
        assertPage( "Continuum - Group Summary" );
        assertTextPresent( "Project Groups" );
        if ( isTextPresent( "No Project Groups Known." ) )
        {
            assertTextNotPresent( "Name" );
            assertTextNotPresent( "Group Id" );
            assertTextNotPresent( "Projects" );
            assertTextNotPresent( "Build Status" );
        }
        else
        {
            assertTextPresent( "Name" );
            assertTextPresent( "Group Id" );
            assertTextPresent( "Projects" );
            assertTextPresent( "Build Status" );
        }
    }
}
