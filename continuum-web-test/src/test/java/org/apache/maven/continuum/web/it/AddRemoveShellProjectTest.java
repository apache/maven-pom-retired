package org.apache.maven.continuum.web.it;

/*
 * Copyright 2004-2005 The Apache Software Foundation.
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
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class AddRemoveShellProjectTest
    extends AbstractIntegrationWebTest
{
    public void testAddRemove()
    {
        addShellProject( "script.sh", "Shell Test Project", "scm:svn:http://svn.apache.org/repos/asf/maven/continuum/trunk/continuum-test-projects/shell", "1.0" );

        assertTextInTable( "projectSummaryTable", "New", 1, 0 );

        assertTextInTable( "projectSummaryTable", "Shell Test Project", 1, 1 );

        assertTextInTable( "projectSummaryTable", "1.0", 1, 2 );

        clickLinkWithText( "Delete" );

        assertTextPresent( "Are you sure you want to delete this project?" );

        submit( "deleteEntry" );
    }
}
