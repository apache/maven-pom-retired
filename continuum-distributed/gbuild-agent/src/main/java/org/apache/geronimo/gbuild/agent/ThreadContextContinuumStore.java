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
public class ThreadContextContinuumStore implements ContinuumStore {

    private static ThreadLocal stores = new ThreadLocal();

    public static ContinuumStore getStore() {
        return (ContinuumStore) stores.get();
    }

    public static void setStore(ContinuumStore store) {
        stores.set(store);
    }

    public Project getProjectByName(String name) throws ContinuumStoreException {
        return getStore().getProjectByName(name);
    }

    public void removeNotifier(ProjectNotifier notifier) throws ContinuumStoreException {
        getStore().removeNotifier(notifier);
    }

    public ProjectNotifier storeNotifier(ProjectNotifier notifier) throws ContinuumStoreException {
        return getStore().storeNotifier(notifier);
    }

    public Map getDefaultBuildDefinitions() {
        return getStore().getDefaultBuildDefinitions();
    }

    public BuildDefinition getDefaultBuildDefinition(int projectId) {
        return getStore().getDefaultBuildDefinition(projectId);
    }

    public BuildDefinition getBuildDefinition(int buildDefinitionId) throws ContinuumStoreException, ContinuumObjectNotFoundException {
        return getStore().getBuildDefinition(buildDefinitionId);
    }

    public void removeBuildDefinition(BuildDefinition buildDefinition) throws ContinuumStoreException {
        getStore().removeBuildDefinition(buildDefinition);
    }

    public BuildDefinition storeBuildDefinition(BuildDefinition buildDefinition) throws ContinuumStoreException {
        return getStore().storeBuildDefinition(buildDefinition);
    }

    public ProjectGroup addProjectGroup(ProjectGroup group) {
        return getStore().addProjectGroup(group);
    }

    public ProjectGroup getProjectGroup(int projectGroupId) throws ContinuumStoreException, ContinuumObjectNotFoundException {
        return getStore().getProjectGroup(projectGroupId);
    }

    public void updateProjectGroup(ProjectGroup group) throws ContinuumStoreException {
        getStore().updateProjectGroup(group);
    }

    public Collection getAllProjectGroupsWithProjects() {
        return getStore().getAllProjectGroupsWithProjects();
    }

    public List getAllProjectsByName() {
        return getStore().getAllProjectsByName();
    }

    public List getAllSchedulesByName() {
        return getStore().getAllSchedulesByName();
    }

    public Schedule addSchedule(Schedule schedule) {
        return getStore().addSchedule(schedule);
    }

    public Schedule getScheduleByName(String name) throws ContinuumStoreException {
        return getStore().getScheduleByName(name);
    }

    public Schedule storeSchedule(Schedule schedule) throws ContinuumStoreException {
        return getStore().storeSchedule(schedule);
    }

    public List getAllProfilesByName() {
        return getStore().getAllProfilesByName();
    }

    public Profile addProfile(Profile profile) {
        return getStore().addProfile(profile);
    }

    public Installation addInstallation(Installation installation) {
        return getStore().addInstallation(installation);
    }

    public List getAllInstallations() {
        return getStore().getAllInstallations();
    }

    public List getAllBuildsForAProjectByDate(int projectId) {
        return getStore().getAllBuildsForAProjectByDate(projectId);
    }

    public Project getProject(int projectId) throws ContinuumStoreException, ContinuumObjectNotFoundException {
        return getStore().getProject(projectId);
    }

    public Map getProjectIdsAndBuildDefinitionIdsBySchedule(int scheduleId) throws ContinuumStoreException {
        return getStore().getProjectIdsAndBuildDefinitionIdsBySchedule(scheduleId);
    }

    public void updateProject(Project project) throws ContinuumStoreException {
        getStore().updateProject(project);
    }

    public void updateProfile(Profile profile) throws ContinuumStoreException {
        getStore().updateProfile(profile);
    }

    public void updateSchedule(Schedule schedule) throws ContinuumStoreException {
        getStore().updateSchedule(schedule);
    }

    public Project getProjectWithBuilds(int projectId) throws ContinuumStoreException, ContinuumObjectNotFoundException {
        return getStore().getProjectWithBuilds(projectId);
    }

    public void removeProfile(Profile profile) {
        getStore().removeProfile(profile);
    }

    public void removeSchedule(Schedule schedule) {
        getStore().removeSchedule(schedule);
    }

    public Project getProjectWithCheckoutResult(int projectId) throws ContinuumObjectNotFoundException, ContinuumStoreException {
        return getStore().getProjectWithCheckoutResult(projectId);
    }

    public BuildResult getBuildResult(int buildId) throws ContinuumObjectNotFoundException, ContinuumStoreException {
        return getStore().getBuildResult(buildId);
    }

    public void removeProject(Project project) {
        getStore().removeProject(project);
    }

    public void removeProjectGroup(ProjectGroup projectGroup) {
        getStore().removeProjectGroup(projectGroup);
    }

    public ProjectGroup getProjectGroupWithBuildDetails(int projectGroupId) throws ContinuumObjectNotFoundException, ContinuumStoreException {
        return getStore().getProjectGroupWithBuildDetails(projectGroupId);
    }

    public List getAllProjectGroupsWithBuildDetails() {
        return getStore().getAllProjectGroupsWithBuildDetails();
    }

    public List getAllProjectsWithAllDetails() {
        return getStore().getAllProjectsWithAllDetails();
    }

    public Project getProjectWithAllDetails(int projectId) throws ContinuumObjectNotFoundException, ContinuumStoreException {
        return getStore().getProjectWithAllDetails(projectId);
    }

    public Schedule getSchedule(int scheduleId) throws ContinuumObjectNotFoundException, ContinuumStoreException {
        return getStore().getSchedule(scheduleId);
    }

    public Profile getProfile(int profileId) throws ContinuumObjectNotFoundException, ContinuumStoreException {
        return getStore().getProfile(profileId);
    }

    public ProjectGroup getProjectGroupByGroupId(String groupId) throws ContinuumStoreException, ContinuumObjectNotFoundException {
        return getStore().getProjectGroupByGroupId(groupId);
    }

    public BuildResult getLatestBuildResultForProject(int projectId) {
        return getStore().getLatestBuildResultForProject(projectId);
    }

    public Map getLatestBuildResults() {
        return getStore().getLatestBuildResults();
    }

    public List getBuildResultByBuildNumber(int projectId, int buildNumber) {
        return getStore().getBuildResultByBuildNumber(projectId, buildNumber);
    }

    public Map getBuildResultsInSuccess() {
        return getStore().getBuildResultsInSuccess();
    }

    public void addBuildResult(Project project, BuildResult build) throws ContinuumStoreException, ContinuumObjectNotFoundException {
        getStore().addBuildResult(project, build);
    }

    public void updateBuildResult(BuildResult build) throws ContinuumStoreException {
        getStore().updateBuildResult(build);
    }

    public Project getProjectWithBuildDetails(int projectId) throws ContinuumObjectNotFoundException, ContinuumStoreException {
        return getStore().getProjectWithBuildDetails(projectId);
    }

    public ProjectGroup getDefaultProjectGroup() throws ContinuumStoreException {
        return getStore().getDefaultProjectGroup();
    }

    public SystemConfiguration addSystemConfiguration(SystemConfiguration systemConf) {
        return getStore().addSystemConfiguration(systemConf);
    }

    public void updateSystemConfiguration(SystemConfiguration systemConf) throws ContinuumStoreException {
        getStore().updateSystemConfiguration(systemConf);
    }

    public SystemConfiguration getSystemConfiguration() throws ContinuumStoreException {
        return getStore().getSystemConfiguration();
    }

    public ContinuumUser addUser(ContinuumUser user) {
        return getStore().addUser(user);
    }

    public void updateUser(ContinuumUser user) throws ContinuumStoreException {
        getStore().updateUser(user);
    }

    public ContinuumUser getUser(int userId) throws ContinuumObjectNotFoundException, ContinuumStoreException {
        return getStore().getUser(userId);
    }

    public ContinuumUser getGuestUser() throws ContinuumStoreException {
        return getStore().getGuestUser();
    }

    public List getUsers() throws ContinuumStoreException {
        return getStore().getUsers();
    }

    public ContinuumUser getUserByUsername(String username) throws ContinuumStoreException {
        return getStore().getUserByUsername(username);
    }

    public void removeUser(ContinuumUser user) {
        getStore().removeUser(user);
    }

    public List getPermissions() throws ContinuumStoreException {
        return getStore().getPermissions();
    }

    public Permission getPermission(String name) throws ContinuumStoreException {
        return getStore().getPermission(name);
    }

    public Permission addPermission(Permission perm) {
        return getStore().addPermission(perm);
    }

    public UserGroup addUserGroup(UserGroup group) {
        return getStore().addUserGroup(group);
    }

    public void updateUserGroup(UserGroup group) throws ContinuumStoreException {
        getStore().updateUserGroup(group);
    }

    public List getUserGroups() throws ContinuumStoreException {
        return getStore().getUserGroups();
    }

    public UserGroup getUserGroup(int userGroupId) throws ContinuumObjectNotFoundException, ContinuumStoreException {
        return getStore().getUserGroup(userGroupId);
    }

    public UserGroup getUserGroup(String name) {
        return getStore().getUserGroup(name);
    }

    public void removeUserGroup(UserGroup group) {
        getStore().removeUserGroup(group);
    }
}
