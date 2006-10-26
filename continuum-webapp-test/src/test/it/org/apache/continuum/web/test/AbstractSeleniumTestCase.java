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

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;
import junit.framework.TestCase;
import org.openqa.selenium.server.SeleniumServer;

import java.util.Calendar;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public abstract class AbstractSeleniumTestCase
    extends TestCase
{
    private Selenium sel;

    protected String adminUsername = "admin";

    protected String adminPassword = "admin1";

    protected String adminFullName = "Continuum Admin";

    protected String adminEmail = "admin@localhost.localdomain.com";

    private String baseUrl = "http://localhost:9595/continuum";

    public void setUp()
    {
        sel = new DefaultSelenium( "localhost", SeleniumServer.DEFAULT_PORT, "*firefox", baseUrl );
        sel.start();
        initialize();
    }

    public void tearDown()
    {
        sel.stop();
    }

    public Selenium getSelenium()
    {
        return sel;
    }

    /**
     * We create an admin user if it doesn't exist
     */
    private void initialize()
    {
        sel.open( "/continuum" );
        waitPage();

        if ( "Create Admin User".equals( sel.getTitle() ) )
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

    public String getHtmlContent()
    {
        return getSelenium().getHtmlSource();
    }

    public void assertTextPresent( String text )
    {
        assertTrue( "'" + text + "' isn't present.", sel.isTextPresent( text ) );
    }

    public void assertTextNotPresent( String text )
    {
        assertFalse( "'" + text + "' is present.", sel.isTextPresent( text ) );
    }

    public void assertElementPresent( String elementLocator )
    {
        assertTrue( "'" + elementLocator + "' isn't present.", sel.isElementPresent( elementLocator ) );
    }

    public void assertElementNotPresent( String elementLocator )
    {
        assertFalse( "'" + elementLocator + "' is present.", sel.isElementPresent( elementLocator ) );
    }

    public void assertLinkPresent( String text )
    {
        assertTrue( "The link '" + text + "' isn't present.", sel.isElementPresent( "link=" + text ) );
    }

    public void assertLinkNotPresent( String text )
    {
        assertFalse( "The link '" + text + "' is present.", sel.isElementPresent( "link=" + text ) );
    }

    public void waitPage()
    {
        waitPage( 30000 );
    }

    public void waitPage( int nbMillisecond )
    {
        sel.waitForPageToLoad( String.valueOf( nbMillisecond ) );
    }

    public void assertPage( String title )
    {
        assertEquals( title, sel.getTitle() );
        assertTrue( sel.getText( "xpath=//div[@id='footer']/table/tbody/tr/td" ).startsWith( "Continuum " ) );
        int currentYear = Calendar.getInstance().get( Calendar.YEAR );
        assertTrue( sel.getText( "xpath=//div[@id='footer']/table/tbody/tr/td" ).endsWith(
            " 2005-" + currentYear + " Apache Software Foundation" ) );
    }

    public void clickLinkWithText( String text )
    {
        clickLinkWithLocator( "link=" + text );
    }

    public void clickLinkWithXPath( String xpath )
    {
        clickLinkWithLocator( "xpath=" + xpath );
    }

    public void clickLinkWithLocator( String locator )
    {
        sel.click( locator );
        waitPage();
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
        sel.type( "user.fullName", fullName );
        sel.type( "user.email", email );
        sel.type( "user.password", password );
        sel.type( "user.confirmPassword", confirmPassword );
        sel.click( "//input[@type='submit']" );
        waitPage();
    }

    //////////////////////////////////////
    // Login
    //////////////////////////////////////
    public void assertLoginPage()
    {
        assertPage( "Login Page" );
        assertTextPresent( "Login" );
        assertTextPresent( "Username" );
        assertTextPresent( "Password" );
        assertTextPresent( "Remember Me" );
        assertFalse( sel.isChecked( "rememberMe" ) );
    }

    public void submitLoginPage( String username, String password )
    {
        submitLoginPage( username, password, false, true );
    }

    public void submitLoginPage( String username, String password, boolean validUsernamePassword )
    {
        submitLoginPage( username, password, false, validUsernamePassword );
    }

    public void submitLoginPage( String username, String password, boolean rememberMe, boolean validUsernamePassword )
    {
        sel.type( "username", username );
        sel.type( "password", password );
        if ( rememberMe )
        {
            sel.check( "rememberMe" );
        }
        sel.click( "submitButton" );
        waitPage();
        if ( validUsernamePassword )
        {
            assertTextPresent( "Welcome, " + username + " - Logout" );
            assertLinkPresent( username );
            assertLinkPresent( "Logout" );
        }
        else
        {
            assertLoginPage();
        }
    }

    public boolean isAuthenticated()
    {
        return !( sel.isElementPresent( "link=Login" ) && sel.isElementPresent( "link=Register" ) );
    }

    //////////////////////////////////////
    // Logout
    //////////////////////////////////////
    public void logout()
    {
        assertTrue( "User wasn't authenticated.", isAuthenticated() );
        clickLinkWithText( "Logout" );
        assertFalse( "The user is always authenticated after a logout.", isAuthenticated() );
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
        sel.type( "baseUrl", baseUrl );
        if ( companyName != null )
        {
            sel.type( "companyName", companyName );
        }
        if ( companyLogo != null )
        {
            sel.type( "companyLogo", companyLogo );
        }
        if ( companyUrl != null )
        {
            sel.type( "companyUrl", companyUrl );
        }
        sel.click( "//input[@type='submit']" );
        waitPage();
    }
}
