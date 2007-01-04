package org.apache.continuum.web.test;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/**
 * Test update or edit account of the current user.
 */
public class MyAccountTest
    extends AbstractAuthenticatedAdminAccessTestCase
{
    public void setUp()
        throws Exception
    {
        super.setUp();
    }

    public void testMyAccountEdit()
        throws Exception
    {
        String newFullName = "Continuum Admin _ new";
        String newEmail = "admin_new@localhost.localdomain.com";
        String newPassword = "admin1_new";
        String newConfirmationPassword = newPassword;

        goToMyAccount();

        // check current account details
        assertMyAccountDetails( adminUsername, adminFullName, adminEmail );

        // change account details
        editMyUserInfo( newFullName, newEmail, newPassword, newConfirmationPassword );
        assertMyAccountDetails( adminUsername, newFullName, newEmail );

        // revert to original account details
        editMyUserInfo( adminFullName, adminEmail, adminPassword, adminPassword );
        assertMyAccountDetails( adminUsername, adminFullName, adminEmail );
    }

}
