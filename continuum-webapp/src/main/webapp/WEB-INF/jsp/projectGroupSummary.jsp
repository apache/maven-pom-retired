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
    <script type="text/javascript">
      function goToAddProject()
      {
        var urls = document.getElementById( "projectTypes" );
        var index = urls.selectedIndex;

        if ( index > 0 )
        {
          var form = document.forms[ "addNewProject" ];
          form.action = urls[ index ].value;

          if ( index == 3 )
          {
            form.projectType.value = "ant";
          }
          else if ( index == 4 )
          {
            form.projectType.value = "shell";
          }
          else
          {
            form.projectType.value = "";
          }

          form.submit();
        }
        else
        {
          alert( "Please choose a project type to add from the dropdown list." );
        }
      }
    </script>
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

      <c:if test="${!empty actionErrors}">
        <div class="errormessage">
          <c:forEach items="${actionErrors}" var="actionError">
            <p><ww:text name="${actionError}"/></p>
          </c:forEach>
        </div>
      </c:if>

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
            <td>
              <pss:ifAuthorized permission="continuum-build-group" resource="${projectGroup.name}">
                <form action="releaseProjectGroup.action" method="post">
                    <input type="hidden" name="projectGroupId" value="<ww:property value="projectGroupId"/>"/>
                    <input type="submit" name="release" value="<ww:text name="release"/>"/>
                </form>
              </pss:ifAuthorized>
            </td>
            <td>
              <pss:ifAnyAuthorized permissions="continuum-add-project-to-group" resource="${projectGroup.name}">
                <ww:form name="addNewProject">
                  <ww:hidden name="disableGroupSelection" value="true"/>
                  <ww:hidden name="selectedProjectGroup" value="${projectGroup.id}"/>
                  <ww:hidden name="projectGroupName" value="${projectGroup.name}"/>
                  <ww:hidden name="projectType" value=""/>
                </ww:form>

                <c:url var="addM2ProjectUrl" value="/addMavenTwoProject!input.action" />
                <c:url var="addM1ProjectUrl" value="/addMavenOneProject!input.action" />
                <c:url var="addProjectUrl" value="/addProjectInput.action" />

                <select id="projectTypes">
                  <option value="">Add New Project</option>
                  <option value="${addM2ProjectUrl}">Add M2 Project</option>
                  <option value="${addM1ProjectUrl}">Add M1 Project</option>
                  <option value="${addProjectUrl}">Add Ant Project</option>
                  <option value="${addProjectUrl}">Add Shell Project</option>
                </select>

                <input type="button" value="Add" onclick="goToAddProject()"/>
              </pss:ifAnyAuthorized>
            </td>
          </tr>
        </table>
      </div>
    </pss:ifAnyAuthorized>

    <ww:action name="projectSummary" executeResult="true" namespace="component">
      <ww:param name="projectGroupId" value="%{projectGroupId}"/>
      <ww:param name="projectGroupName" value="%{projectGroup.name}"/>
    </ww:action>

  </div>
  </body>
</ww:i18n>
</html>
