package org.apache.maven.continuum.security;
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
 * ContinuumRoleConstants:
 *
 * @author: Jesse McConnell <jmcconnell@apache.org>
 * @version: $ID:$
 */
public class ContinuumRoleConstants
{
    public static final String DELIMITER = " - ";

    // globalish roles
    public static final String SYSTEM_ADMINISTRATOR_ROLE = "System Administrator";
    public static final String USER_ADMINISTRATOR_ROLE = "User Administrator";
    public static final String GROUP_ADMINISTRATOR_ROLE = "Continuum Group Project Administrator";
    public static final String REGISTERED_USER_ROLE = "Registered User";
    public static final String GUEST_ROLE = "Guest";

    // roles
    public static final String CONTINUUM_PROJECT_ADMINISTRATOR_ROLE_PREFIX = "Project Administrator";
    public static final String CONTINUUM_PROJECT_DEVELOPER_ROLE_PREFIX = "Project Developer";
    public static final String CONTINUUM_PROJECT_USER_ROLE_PREFIX = "Project User";

    // global operations
    public static final String CONTINUUM_MANAGE_SCHEDULES = "continuum-manage-schedules";
    public static final String CONTINUUM_MANAGE_CONFIGURATION = "continuum-manage-configuration";
    public static final String CONTINUUM_MANAGE_USERS = "continuum-manage-users";
    public static final String CONTINUUM_ACTIVE_GUEST_OPERATION = "continuum-guest";

    // dynamic operations
    public static final String CONTINUUM_VIEW_GROUP_OPERATION = "continuum-view-group";
    public static final String CONTINUUM_ADD_GROUP_OPERATION = "continuum-add-group";
    public static final String CONTINUUM_REMOVE_GROUP_OPERATION = "continuum-remove-group";
    public static final String CONTINUUM_BUILD_GROUP_OPERATION = "continuum-build-group";
    public static final String CONTINUUM_MODIFY_GROUP_OPERATION = "continuum-modify-group";
    public static final String CONTINUUM_ADD_PROJECT_TO_GROUP_OPERATION = "continuum-add-project-to-group";
    public static final String CONTINUUM_REMOVE_PROJECT_FROM_GROUP_OPERATION ="continuum-remove-project-from-group";
    public static final String CONTINUUM_MODIFY_PROJECT_IN_GROUP_OPERATION = "continuum-modify-project-in-group";
    public static final String CONTINUUM_BUILD_PROJECT_IN_GROUP_OPERATION = "continuum-build-project-in-group";
    public static final String CONTINUUM_ADD_GROUP_BUILD_DEFINTION_OPERATION = "continuum-add-group-build-definition";
    public static final String CONTINUUM_REMOVE_GROUP_BUILD_DEFINITION_OPERATION = "continuum-remove-group-build-definition";
    public static final String CONTINUUM_MODIFY_GROUP_BUILD_DEFINITION_OPERATION = "continuum-modify-group-build-definition";
    public static final String CONTINUUM_ADD_GROUP_NOTIFIER_OPERATION = "continuum-add-group-notifier";
    public static final String CONTINUUM_REMOVE_GROUP_NOTIFIER_OPERATION = "continuum-remove-group-notifier";
    public static final String CONTINUUM_MODIFY_GROUP_NOTIFIER_OPERATION = "continuum-modify-group-notifier";
    public static final String CONTINUUM_ADD_PROJECT_BUILD_DEFINTION_OPERATION = "continuum-add-project-build-definition";
    public static final String CONTINUUM_REMOVE_PROJECT_BUILD_DEFINITION_OPERATION = "continuum-remove-project-build-definition";
    public static final String CONTINUUM_MODIFY_PROJECT_BUILD_DEFINITION_OPERATION = "continuum-modify-project-build-definition";
    public static final String CONTINUUM_ADD_PROJECT_NOTIFIER_OPERATION = "continuum-add-project-notifier";
    public static final String CONTINUUM_REMOVE_PROJECT_NOTIFIER_OPERATION = "continuum-remove-project-notifier";
    public static final String CONTINUUM_MODIFY_PROJECT_NOTIFIER_OPERATION = "continuum-modify-project-notifier";

    // operations against user assignment.
    public static final String USER_MANAGEMENT_ROLE_GRANT_OPERATION = "user-management-role-grant";
    public static final String USER_MANAGEMENT_USER_ROLE_OPERATION = "user-management-user-role";

}
