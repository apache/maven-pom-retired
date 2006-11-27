/**
 *
 * Copyright 2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.gbuild.agent;

import org.apache.maven.continuum.store.ContinuumStore;
import org.apache.maven.continuum.store.ContinuumStoreException;
import org.apache.maven.continuum.store.ContinuumObjectNotFoundException;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.model.project.BuildDefinition;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.model.project.Schedule;
import org.apache.maven.continuum.model.project.Profile;
import org.apache.maven.continuum.model.project.BuildResult;
import org.apache.maven.continuum.model.system.Installation;
import org.apache.maven.continuum.model.system.SystemConfiguration;
import org.apache.maven.continuum.model.system.ContinuumUser;
import org.apache.maven.continuum.model.system.Permission;
import org.apache.maven.continuum.model.system.UserGroup;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
public class MockContinuumStore implements ContinuumStore {

    public Project getProjectByName(String name) throws ContinuumStoreException {
        throw new UnsupportedOperationException("Method getProjectByName not implemented");
    }

    public void removeNotifier(ProjectNotifier notifier) throws ContinuumStoreException {
        throw new UnsupportedOperationException("Method removeNotifier not implemented");
    }

    public ProjectNotifier storeNotifier(ProjectNotifier notifier) throws ContinuumStoreException {
        throw new UnsupportedOperationException("Method storeNotifier not implemented");
    }

    public BuildDefinition getBuildDefinition(int buildDefinitionId) throws ContinuumStoreException, ContinuumObjectNotFoundException {
        throw new UnsupportedOperationException("Method getBuildDefinition not implemented");
    }

    public void removeBuildDefinition(BuildDefinition buildDefinition) throws ContinuumStoreException {
        throw new UnsupportedOperationException("Method removeBuildDefinition not implemented");
    }

    public BuildDefinition storeBuildDefinition(BuildDefinition buildDefinition) throws ContinuumStoreException {
        throw new UnsupportedOperationException("Method storeBuildDefinition not implemented");
    }

    public ProjectGroup addProjectGroup(ProjectGroup group) {
        throw new UnsupportedOperationException("Method addProjectGroup not implemented");
    }

    public ProjectGroup getProjectGroup(int projectGroupId) throws ContinuumStoreException, ContinuumObjectNotFoundException {
        throw new UnsupportedOperationException("Method getProjectGroup not implemented");
    }

    public void updateProjectGroup(ProjectGroup group) throws ContinuumStoreException {
        throw new UnsupportedOperationException("Method updateProjectGroup not implemented");
    }

    public Collection getAllProjectGroupsWithProjects() {
        throw new UnsupportedOperationException("Method getAllProjectGroupsWithProjects not implemented");
    }

    public List getAllProjectsByName() {
        throw new UnsupportedOperationException("Method getAllProjectsByName not implemented");
    }

    public List getAllSchedulesByName() {
        throw new UnsupportedOperationException("Method getAllSchedulesByName not implemented");
    }

    public Schedule addSchedule(Schedule schedule) {
        throw new UnsupportedOperationException("Method addSchedule not implemented");
    }

    public Schedule getScheduleByName(String name) throws ContinuumStoreException {
        throw new UnsupportedOperationException("Method getScheduleByName not implemented");
    }

    public Schedule storeSchedule(Schedule schedule) throws ContinuumStoreException {
        throw new UnsupportedOperationException("Method storeSchedule not implemented");
    }

    public List getAllProfilesByName() {
        throw new UnsupportedOperationException("Method getAllProfilesByName not implemented");
    }

    public Profile addProfile(Profile profile) {
        throw new UnsupportedOperationException("Method addProfile not implemented");
    }

    public Installation addInstallation(Installation installation) {
        throw new UnsupportedOperationException("Method addInstallation not implemented");
    }

    public List getAllInstallations() {
        throw new UnsupportedOperationException("Method getAllInstallations not implemented");
    }

    public List getAllBuildsForAProjectByDate(int projectId) {
        throw new UnsupportedOperationException("Method getAllBuildsForAProjectByDate not implemented");
    }

    public Project getProject(int projectId) throws ContinuumStoreException, ContinuumObjectNotFoundException {
        throw new UnsupportedOperationException("Method getProject not implemented");
    }

    public void updateProject(Project project) throws ContinuumStoreException {
        throw new UnsupportedOperationException("Method updateProject not implemented");
    }

    public void updateProfile(Profile profile) throws ContinuumStoreException {
        throw new UnsupportedOperationException("Method updateProfile not implemented");
    }

    public void updateSchedule(Schedule schedule) throws ContinuumStoreException {
        throw new UnsupportedOperationException("Method updateSchedule not implemented");
    }

    public Project getProjectWithBuilds(int projectId) throws ContinuumStoreException, ContinuumObjectNotFoundException {
        throw new UnsupportedOperationException("Method getProjectWithBuilds not implemented");
    }

    public void removeProfile(Profile profile) {
        throw new UnsupportedOperationException("Method removeProfile not implemented");
    }

    public void removeSchedule(Schedule schedule) {
        throw new UnsupportedOperationException("Method removeSchedule not implemented");
    }

    public Project getProjectWithCheckoutResult(int projectId) throws ContinuumObjectNotFoundException, ContinuumStoreException {
        throw new UnsupportedOperationException("Method getProjectWithCheckoutResult not implemented");
    }

    public BuildResult getBuildResult(int buildId) throws ContinuumObjectNotFoundException, ContinuumStoreException {
        throw new UnsupportedOperationException("Method getBuildResult not implemented");
    }

    public void removeProject(Project project) {
        throw new UnsupportedOperationException("Method removeProject not implemented");
    }

    public void removeProjectGroup(ProjectGroup projectGroup) {
        throw new UnsupportedOperationException("Method removeProjectGroup not implemented");
    }

    public ProjectGroup getProjectGroupWithBuildDetails(int projectGroupId) throws ContinuumObjectNotFoundException, ContinuumStoreException {
        throw new UnsupportedOperationException("Method getProjectGroupWithBuildDetails not implemented");
    }

    public List getAllProjectGroupsWithBuildDetails() {
        throw new UnsupportedOperationException("Method getAllProjectGroupsWithBuildDetails not implemented");
    }

    public List getAllProjectsWithAllDetails() {
        throw new UnsupportedOperationException("Method getAllProjectsWithAllDetails not implemented");
    }

    public Project getProjectWithAllDetails(int projectId) throws ContinuumObjectNotFoundException, ContinuumStoreException {
        throw new UnsupportedOperationException("Method getProjectWithAllDetails not implemented");
    }

    public Schedule getSchedule(int scheduleId) throws ContinuumObjectNotFoundException, ContinuumStoreException {
        throw new UnsupportedOperationException("Method getSchedule not implemented");
    }

    public Profile getProfile(int profileId) throws ContinuumObjectNotFoundException, ContinuumStoreException {
        throw new UnsupportedOperationException("Method getProfile not implemented");
    }

    public ProjectGroup getProjectGroupByGroupId(String groupId) throws ContinuumStoreException, ContinuumObjectNotFoundException {
        throw new UnsupportedOperationException("Method getProjectGroupByGroupId not implemented");
    }

    public BuildResult getLatestBuildResultForProject(int projectId) {
        throw new UnsupportedOperationException("Method getLatestBuildResultForProject not implemented");
    }

    public void addBuildResult(Project project, BuildResult build) throws ContinuumStoreException, ContinuumObjectNotFoundException {
        throw new UnsupportedOperationException("Method addBuildResult not implemented");
    }

    public void updateBuildResult(BuildResult build) throws ContinuumStoreException {
        throw new UnsupportedOperationException("Method updateBuildResult not implemented");
    }

    public Project getProjectWithBuildDetails(int projectId) throws ContinuumObjectNotFoundException, ContinuumStoreException {
        throw new UnsupportedOperationException("Method getProjectWithBuildDetails not implemented");
    }

    public ProjectGroup getDefaultProjectGroup() throws ContinuumStoreException {
        throw new UnsupportedOperationException("Method getDefaultProjectGroup not implemented");
    }

    public SystemConfiguration addSystemConfiguration(SystemConfiguration systemConf) {
        throw new UnsupportedOperationException("Method addSystemConfiguration not implemented");
    }

    public void updateSystemConfiguration(SystemConfiguration systemConf) throws ContinuumStoreException {
        throw new UnsupportedOperationException("Method updateSystemConfiguration not implemented");
    }

    public SystemConfiguration getSystemConfiguration() throws ContinuumStoreException {
        throw new UnsupportedOperationException("Method getSystemConfiguration not implemented");
    }

    public ContinuumUser addUser(ContinuumUser user) {
        throw new UnsupportedOperationException("Method addUser not implemented");
    }

    public void updateUser(ContinuumUser user) throws ContinuumStoreException {
        throw new UnsupportedOperationException("Method updateUser not implemented");
    }

    public ContinuumUser getUser(int userId) throws ContinuumObjectNotFoundException, ContinuumStoreException {
        throw new UnsupportedOperationException("Method getUser not implemented");
    }

    public ContinuumUser getGuestUser() throws ContinuumStoreException {
        throw new UnsupportedOperationException("Method getGuestUser not implemented");
    }

    public List getUsers() throws ContinuumStoreException {
        throw new UnsupportedOperationException("Method getUsers not implemented");
    }

    public ContinuumUser getUserByUsername(String username) throws ContinuumStoreException {
        throw new UnsupportedOperationException("Method getUserByUsername not implemented");
    }

    public void removeUser(ContinuumUser user) {
        throw new UnsupportedOperationException("Method removeUser not implemented");
    }

    public List getPermissions() throws ContinuumStoreException {
        throw new UnsupportedOperationException("Method getPermissions not implemented");
    }

    public Permission getPermission(String name) throws ContinuumStoreException {
        throw new UnsupportedOperationException("Method getPermission not implemented");
    }

    public Permission addPermission(Permission perm) {
        throw new UnsupportedOperationException("Method addPermission not implemented");
    }

    public UserGroup addUserGroup(UserGroup group) {
        throw new UnsupportedOperationException("Method addUserGroup not implemented");
    }

    public void updateUserGroup(UserGroup group) throws ContinuumStoreException {
        throw new UnsupportedOperationException("Method updateUserGroup not implemented");
    }

    public List getUserGroups() throws ContinuumStoreException {
        throw new UnsupportedOperationException("Method getUserGroups not implemented");
    }

    public UserGroup getUserGroup(int userGroupId) throws ContinuumObjectNotFoundException, ContinuumStoreException {
        throw new UnsupportedOperationException("Method getUserGroup not implemented");
    }

    public UserGroup getUserGroup(String name) {
        throw new UnsupportedOperationException("Method getUserGroup not implemented");
    }

    public void removeUserGroup(UserGroup group) {
        throw new UnsupportedOperationException("Method removeUserGroup not implemented");
    }

    public Map getDefaultBuildDefinitions() {
        throw new UnsupportedOperationException("Method getDefaultBuildDefinitions not implemented");
    }

    public BuildDefinition getDefaultBuildDefinition(int projectId) {
        throw new UnsupportedOperationException("Method getDefaultBuildDefinition not implemented");
    }

    public Map getProjectIdsAndBuildDefinitionIdsBySchedule(int scheduleId) throws ContinuumStoreException {
        throw new UnsupportedOperationException("Method getProjectIdsAndBuildDefinitionIdsBySchedule not implemented");
    }

    public Map getLatestBuildResults() {
        throw new UnsupportedOperationException("Method getLatestBuildResults not implemented");
    }

    public List getBuildResultByBuildNumber(int projectId, int buildNumber) {
        throw new UnsupportedOperationException("Method getBuildResultByBuildNumber not implemented");
    }

    public Map getBuildResultsInSuccess() {
        throw new UnsupportedOperationException("Method getBuildResultsInSuccess not implemented");
    }
}
