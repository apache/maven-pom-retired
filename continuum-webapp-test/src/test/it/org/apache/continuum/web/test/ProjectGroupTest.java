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
 * Test case for project groups.
 */
public class ProjectGroupTest
    extends AbstractAuthenticatedAdminAccessTestCase
{
    public void testAddRemoveProjectGroup()
        throws Exception
    {
        addProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );

        showProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );

        removeProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
    }

    public void testDefaultBuildDefinition()
        throws Exception
    {
        goToProjectGroupsSummaryPage();

        showProjectGroup( DEFAULT_PROJ_GRP_NAME, DEFAULT_PROJ_GRP_ID, DEFAULT_PROJ_GRP_DESCRIPTION );

        clickLinkWithText( "Build Definitions" );

        String tableElement = "ec_table";
        assertCellValueFromTable( "Goals", tableElement, 0, 0 );
        assertCellValueFromTable( "Arguments", tableElement, 0, 1 );
        assertCellValueFromTable( "Build File", tableElement, 0, 2 );
        assertCellValueFromTable( "schedule", tableElement, 0, 3 );
        assertCellValueFromTable( "From", tableElement, 0, 4 );
        assertCellValueFromTable( "Default", tableElement, 0, 5 );
        assertCellValueFromTable( "", tableElement, 0, 6 );
        assertCellValueFromTable( "", tableElement, 0, 7 );

        assertCellValueFromTable( "clean install", tableElement, 1, 0 );
        assertCellValueFromTable( "--batch-mode --non-recursive", tableElement, 1, 1 );
        assertCellValueFromTable( "pom.xml", tableElement, 1, 2 );
        assertCellValueFromTable( "DEFAULT_SCHEDULE", tableElement, 1, 3 );
        assertCellValueFromTable( "GROUP", tableElement, 1, 4 );
        assertCellValueFromTable( "true", tableElement, 1, 5 );
        assertImgWithAlt( "Edit" );
        assertImgWithAlt( "Delete" );
    }

    public void testMoveProject()
        throws Exception
    {
        // Add a project group and a project to it
        addProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION );
        addMavenTwoProject( TEST_POM_URL, TEST_POM_USERNAME, TEST_POM_PASSWORD, TEST_PROJ_GRP_NAME, true );

        // assert that the default project group has 0 projects while the test project group has 1
        assertCellValueFromTable( "0", "ec_table", 1, 2 );
        assertCellValueFromTable( "1", "ec_table", 2, 2 );

        // move the project of the test project group to the default project group
        moveProjectToProjectGroup( TEST_PROJ_GRP_NAME, TEST_PROJ_GRP_ID, TEST_PROJ_GRP_DESCRIPTION,
                                   DEFAULT_PROJ_GRP_NAME );

        // assert that the default project group now has 1 while the test project group has 0
        goToProjectGroupsSummaryPage();
        assertCellValueFromTable( "1", "ec_table", 1, 2 );
        assertCellValueFromTable( "0", "ec_table", 2, 2 );
    }

}
