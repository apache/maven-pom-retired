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


import org.apache.maven.shared.web.test.XPathExpressionUtil;

import java.util.HashMap;

/**
 *
 *
 *
 */
public class SchedulesPageTest
    extends AbstractAuthenticatedAccessTestCase
{
    // Add Edit Page fields
    final public static String FIELD_NAME = "name";

    final public static String FIELD_DESCRIPTION = "description";

    final public static String FIELD_SECOND = "second";

    final public static String FIELD_MINUTE = "minute";

    final public static String FIELD_HOUR = "hour";

    final public static String FIELD_DAYOFMONTH = "dayOfMonth";

    final public static String FIELD_MONTH = "month";

    final public static String FIELD_DAYOFWEEK = "dayOfWeek";

    final public static String FIELD_YEAR = "year";

    final public static String FIELD_MAXJOBEXECUTIONTIME = "maxJobExecutionTime";

    final public static String FIELD_DELAY = "delay";


    // field values
    final public static String SCHEDULES_PAGE_TITLE = "Continuum - Schedules";

    final public static String DEFAULT_SCHEDULE = "DEFAULT_SCHEDULE";

    final public static String DEFAULT_SCHEDULE_DESCRIPTION = "Run hourly";

    final public static String DEFAULT_CRONVALUE = "0 0 * * * ?";

    final public static String DEFAULT_DELAY = "0";

    final public static String DEFAULT_MAXJOBEXECUTIONTIME = "0";

    final public static String EDIT_SCHEDULE_PAGE_TITLE = "Continuum - Edit Schedule";

    final public static String SCHEDULE_NAME = "Test Schedule";

    final public static String SCHEDULE_NAME_EDIT = "Test Schedule Edit";

    final public static String SCHEDULE_DESCRIPTION = "Test Description";

    final public static String SECOND = "1";

    final public static String MINUTE = "2";

    final public static String HOUR = "3";

    final public static String DAYOFMONTH = "?";

    final public static String MONTH = "4";

    final public static String DAYOFWEEK = "5";

    final public static String YEAR = "2020";

    final public static String MAXJOBEXECUTIONTIME = "6";

    final public static String DELAY = "7";

    public void setUp()
        throws Exception
    {
        super.setUp();

        clickLinkWithText( "Schedules" );

        assertSchedulesPage();
    }

    public String getUsername()
    {
        return this.adminUsername;
    }

    public String getPassword()
    {
        return this.adminPassword;
    }

    public void testBasicScheduleAddAndDelete()
        throws Exception
    {
        // add schedule
        clickButtonWithValue( "Add" );

        assertEditSchedulePage();

        inputSchedule( SCHEDULE_NAME, SCHEDULE_DESCRIPTION, SECOND, MINUTE, HOUR, DAYOFMONTH, MONTH, DAYOFWEEK, YEAR,
                       MAXJOBEXECUTIONTIME, DELAY, true );

        String cronSchedule = SECOND;
        cronSchedule += " " + MINUTE;
        cronSchedule += " " + HOUR;
        cronSchedule += " " + DAYOFMONTH;
        cronSchedule += " " + MONTH;
        cronSchedule += " " + DAYOFWEEK;
        cronSchedule += " " + YEAR;

        String[] columnValues = {SCHEDULE_NAME, SCHEDULE_DESCRIPTION, DELAY, cronSchedule, MAXJOBEXECUTIONTIME};

        assertTrue( "Can not add schedule",
                    getSelenium().isElementPresent( XPathExpressionUtil.getTableRow( columnValues ) ) );

        // delete schedule after adding
        deleteSchedule( SCHEDULE_NAME );

        assertFalse( "Can not delete schedule",
                     getSelenium().isElementPresent( XPathExpressionUtil.getTableRow( columnValues ) ) );
    }

    public void testEditSchedule()
        throws Exception
    {
        clickButtonWithValue( "Add" );

        assertEditSchedulePage();

        inputSchedule( SCHEDULE_NAME_EDIT, SCHEDULE_DESCRIPTION, SECOND, MINUTE, HOUR, DAYOFMONTH, MONTH, DAYOFWEEK,
                       YEAR, MAXJOBEXECUTIONTIME, DELAY, true );

        String cronSchedule = SECOND;
        cronSchedule += " " + MINUTE;
        cronSchedule += " " + HOUR;
        cronSchedule += " " + DAYOFMONTH;
        cronSchedule += " " + MONTH;
        cronSchedule += " " + DAYOFWEEK;
        cronSchedule += " " + YEAR;

        String[] columnValues = {SCHEDULE_NAME, SCHEDULE_DESCRIPTION, DELAY, cronSchedule, MAXJOBEXECUTIONTIME};

        // edit the schedule        
        clickLinkWithXPath( XPathExpressionUtil.getColumnElement( XPathExpressionUtil.ANCHOR, 5,
                                                                                      "Edit", columnValues ) );

        inputSchedule( SCHEDULE_NAME_EDIT + "modified", SCHEDULE_DESCRIPTION + "updated", "2", "3", "4", "?", "6", "7",
                       "2021", "8", "9", false );

        cronSchedule = "2 3 4 ? 6 7 2021";

        String[] editedColumnValues =
            {SCHEDULE_NAME_EDIT + "modified", SCHEDULE_DESCRIPTION + "updated", "9", cronSchedule, "8"};

        assertTrue( "Can not edit schedule", getSelenium().isElementPresent(
            XPathExpressionUtil.getTableRow( editedColumnValues ) ) );

        // check if the active state has been saved
        clickLinkWithXPath( XPathExpressionUtil.getColumnElement( XPathExpressionUtil.ANCHOR, 5,
                                                                                      "Edit", editedColumnValues ) );

        assertEquals( "Can disable the schedule", CHECKBOX_UNCHECK, getFieldValue( "active" ) );

        //house keeping
        clickLinkWithText( "Schedules" );
        deleteSchedule( SCHEDULE_NAME_EDIT + "modified" );
    }

    public void testScheduleAddEditPageInputValidation()
    {
        clickButtonWithValue( "Add" );

        assertEditSchedulePage();

        HashMap fields = new HashMap();
        boolean valid = false;
        boolean wait = false;

        // test saving without editing anything from the initial edit page
        inputSchedule( fields, wait, valid );

        assertTrue( "Name field not validated",
                    getSelenium().isElementPresent( "//tr/td[span='schedule.name.required']" ) );
        assertTrue( "Description field not validated",
                    getSelenium().isElementPresent( "//tr/td[span='schedule.version.required']" ) );

        // go back to the schedules page
        clickLinkWithText( "Schedules" );

        // start new schedule add session
        clickButtonWithValue( "Add" );

        // test saving using alpha characters for the maxjobexecution and delay
        // with valid name and description  
        fields.put( FIELD_NAME, SCHEDULE_NAME );
        fields.put( FIELD_DESCRIPTION, SCHEDULE_DESCRIPTION );
        fields.put( FIELD_MAXJOBEXECUTIONTIME, "abcde" );
        fields.put( FIELD_DELAY, "abcde" );

        inputSchedule( fields, wait, valid );

        //TODO: Fix text validation, we need real text and not a property in the screen
        assertFalse( "Name field improperly validated",
                     getSelenium().isElementPresent( "//tr/td[span='schedule.name.required']" ) );
        assertFalse( "Description field improperly validated",
                     getSelenium().isElementPresent( "//tr/td[span='schedule.name.required']" ) );
        assertTrue( "MaxJobExecutionTime not validated", isTextPresent( "schedule.maxJobExecutionTime.invalid" ) );
        assertTrue( "Delay not validated", isTextPresent( "schedule.delay.invalid" ) );

        assertEditSchedulePage();
    }


    public void assertSchedulesPage()
    {
        assertPage( SCHEDULES_PAGE_TITLE );

        assertDefaultSchedule();
    }

    public void assertDefaultSchedule()
    {
        String[] columnValues = {DEFAULT_SCHEDULE, DEFAULT_SCHEDULE_DESCRIPTION, DEFAULT_DELAY, DEFAULT_CRONVALUE,
            DEFAULT_MAXJOBEXECUTIONTIME};

        assertTrue( "Default schedule not found",
                    getSelenium().isElementPresent( XPathExpressionUtil.getTableRow( columnValues ) ) );
    }

    public void assertEditSchedulePage()
    {
        assertPage( EDIT_SCHEDULE_PAGE_TITLE );

        //TODO: assert error messages

        assertEditSchedulePageInputFields();
    }

    public void assertEditSchedulePageInputFields()
    {
        //TODO: assert content

        assertElementPresent( "saveSchedule_id" );
        assertElementPresent( "id" );
        assertElementPresent( "saveSchedule_name" );
        assertElementPresent( "name" );
        assertElementPresent( "saveSchedule_description" );
        assertElementPresent( "description" );
        assertElementPresent( "saveSchedule_second" );
        assertElementPresent( "second" );
        assertElementPresent( "saveSchedule_minute" );
        assertElementPresent( "minute" );
        assertElementPresent( "saveSchedule_hour" );
        assertElementPresent( "hour" );
        assertElementPresent( "saveSchedule_dayOfMonth" );
        assertElementPresent( "dayOfMonth" );
        assertElementPresent( "saveSchedule_month" );
        assertElementPresent( "month" );
        assertElementPresent( "saveSchedule_dayOfWeek" );
        assertElementPresent( "dayOfWeek" );
        assertElementPresent( "saveSchedule_year" );
        assertElementPresent( "year" );
        assertElementPresent( "saveSchedule_maxJobExecutionTime" );
        assertElementPresent( "maxJobExecutionTime" );
        assertElementPresent( "saveSchedule_delay" );
        assertElementPresent( "delay" );
        assertElementPresent( "saveSchedule_active" );
        assertElementPresent( "active" );
    }

    public void deleteSchedule( String scheduleName )
    {
        // after we save the schedule we should be brought back to the schedules page
        assertSchedulesPage();

        String[] columnValues = {scheduleName};

        clickLinkWithXPath( XPathExpressionUtil.getColumnElement( XPathExpressionUtil.ANCHOR, 5,
                                                                                      "Delete", columnValues ) );

        // deletion confirmation page
        assertPage( "Schedule Removal" );
        //TODO: assert content
        //TODO: assert schedule name is in deletion confirmation

        clickButtonWithValue( "Delete" );

        // after we confirm the deletion we should be brought back to the schedules page
        assertSchedulesPage();
    }


    public void inputSchedule( String scheduleName, String scheduleDescription, String second, String minute,
                               String hour, String dayOfMonth, String month, String dayOfWeek, String year,
                               String maxJobExecutionTime, String delay, boolean active )
    {
        inputSchedule( scheduleName, scheduleDescription, second, minute, hour, dayOfMonth, month, dayOfWeek, year,
                       maxJobExecutionTime, delay, active, true );
    }


    public void inputSchedule( String scheduleName, String schedule_description, String second, String minute,
                               String hour, String dayOfMonth, String month, String dayOfWeek, String year,
                               String maxJobExecutionTime, String delay, boolean active, boolean wait )
    {
        assertEditSchedulePage();

        HashMap inputFields = new HashMap();

        inputFields.put( "name", scheduleName );
        inputFields.put( "description", schedule_description );
        inputFields.put( "second", second );
        inputFields.put( "minute", minute );
        inputFields.put( "hour", hour );
        inputFields.put( "dayOfMonth", dayOfMonth );
        inputFields.put( "month", month );
        inputFields.put( "dayOfWeek", dayOfWeek );
        inputFields.put( "year", year );
        inputFields.put( "maxJobExecutionTime", maxJobExecutionTime );
        inputFields.put( "delay", delay );

        if ( !active )
        {
            uncheckField( "active" );
        }

        inputSchedule( inputFields, wait, true );
    }

    public void inputSchedule( HashMap fields )
    {
        inputSchedule( fields, true, true );
    }

    public void inputSchedule( HashMap fields, boolean wait, boolean valid )
    {
        setFieldValues( fields );

        clickButtonWithValue( "Save", wait );

        if ( valid )
        {
            // after we save the schedule we should be brought back to the schedules page        
            assertSchedulesPage();
        }
        else
        {
            assertEditSchedulePage();
        }
    }

    public void tearDown()
    {
        logout();

        super.tearDown();
    }
}
