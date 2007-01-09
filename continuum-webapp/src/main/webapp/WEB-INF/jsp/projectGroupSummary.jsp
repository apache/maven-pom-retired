<%--
  ~ Licensed to the Apache Software Foundation (ASF) under one
  ~ or more contributor license agreements.  See the NOTICE file
  ~ distributed with this work for additional information
  ~ regarding copyright ownership.  The ASF licenses this file
  ~ to you under the Apache License, Version 2.0 (the
  ~ "License"); you may not use this file except in compliance
  ~ with the License.  You may obtain a copy of the License at
  ~
  ~   http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied.  See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  --%>

<%@ taglib uri="/webwork" prefix="ww" %>
<%@ taglib uri="http://www.extremecomponents.org" prefix="ec" %>
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c' %>
<%@ taglib uri="continuum" prefix="c1" %>
<%@ taglib uri="/plexusSecuritySystem" prefix="pss" %>

<html>

<ww:i18n name="localization.Continuum">
  <head>
    <title>
      <ww:text name="projectGroup.page.title"/>
    </title>
  </head>
  <body>
  <div id="h3">
    <ww:action name="projectGroupTab" executeResult="true">
      <ww:param name="tabName" value="'Summary'"/>
    </ww:action>

    <h3>Project Group</h3>
    <div class="axial">
      <table border="1" cellspacing="2" cellpadding="3" width="100%">
        <c1:data label="%{getText('projectGroup.name.label')}" name="projectGroup.name"/>
        <c1:data label="%{getText('projectGroup.groupId.label')}" name="projectGroup.groupId"/>
        <c1:data label="%{getText('projectGroup.description.label')}" name="projectGroup.description"/>
      </table>
    </div>

    <pss:ifAnyAuthorized permissions="continuum-build-group,continuum-remove-group" resource="${projectGroup.name}">
      <h3>Project Group Actions</h3>

      <div class="functnbar3">
        <table>
          <tr>
            <td>
              <pss:ifAuthorized permission="continuum-build-group" resource="${projectGroup.name}">
                <form action="buildProjectGroup.action" method="post">
                    <input type="hidden" name="projectGroupId" value="<ww:property value="projectGroupId"/>"/>
                    <input type="submit" name="build" value="<ww:text name="build"/>"/>
                </form>
                </pss:ifAuthorized>
            <td>
                <pss:ifAuthorized permission="continuum-modify-group" resource="${projectGroup.name}">
                <form action="editProjectGroup.action" method="post">
                    <input type="hidden" name="projectGroupId" value="<ww:property value="projectGroupId"/>"/>
                    <input type="submit" name="edit" value="<ww:text name="edit"/>"/>
                </form>
                </pss:ifAuthorized>
            </td>
            <td>
              <pss:ifAuthorized permission="continuum-remove-group" resource="${projectGroup.name}">
                <form action="removeProjectGroup.action" method="post">
                    <input type="hidden" name="projectGroupId" value="<ww:property value="projectGroupId"/>"/>
                    <input type="submit" name="remove" value="<ww:text name="delete"/>"/>
                </form>
              </pss:ifAuthorized>
            </td>
          </tr>
        </table>
      </div>
    </pss:ifAnyAuthorized>
   
    <ww:action name="projectSummary" executeResult="true" namespace="component">
      <ww:param name="projectGroupId" value="%{projectGroupId}"/>
      <ww:param name="projectGroupName" value="%{projectGroup.name}"/>
    </ww:action>
    
    <pss:ifAnyAuthorized permissions="continuum-add-project-to-group" resource="${projectGroup.name}">
      <div class="functnbar3">
        <c:url var="addM2ProjectUrl" value="/addMavenTwoProject!default.action">
          <c:param name="disableGroupSelection" value="true"/>
          <c:param name="selectedProjectGroup" value="${projectGroup.id}"/>
          <c:param name="projectGroupName" value="${projectGroup.name}"/>
        </c:url>
        <c:url var="addM1ProjectUrl" value="/addMavenOneProject!default.action">
          <c:param name="disableGroupSelection" value="true"/>
          <c:param name="selectedProjectGroup" value="${projectGroup.id}"/>
          <c:param name="projectGroupName" value="${projectGroup.name}"/>
        </c:url>
        <table>
          <tr>
            <td>
              <form action="${addM2ProjectUrl}" method="post">
                <input type="submit" name="addM2Project" value="<ww:text name="add.m2.project.section.title"/>"/>
              </form>
            </td>
            <td>
              <form action="${addM1ProjectUrl}" method="post">
                <input type="submit" name="addM1Project" value="<ww:text name="add.m1.project.section.title"/>"/>
              </form>
            </td>
          </tr>
        </table>
      </div>
    </pss:ifAnyAuthorized>

  </div>
  </body>
</ww:i18n>
</html>
