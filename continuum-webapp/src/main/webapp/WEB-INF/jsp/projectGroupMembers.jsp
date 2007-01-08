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
      <title><ww:text name="projectGroup.page.title"/></title>
    </head>

    <body>
      <div id="h3">

    <ww:action name="projectGroupTab" executeResult="true">
      <ww:param name="tabName" value="'Members'"/>
    </ww:action>
    <div class="axial">
      <!--
      Scan for new Projects?
      -->
    </div>

    <h3>Member Projects of ${projectGroup.name} group</h3>

    <ec:table items="groupProjects"
              var="project"
              showExports="false"
              showPagination="false"
              showStatusBar="false"
              filterable="false"
              sortable="false">
      <ec:row highlightRow="true">
        <ec:column property="name" title="summary.projectTable.name" width="48%">
          <ww:url id="projectViewUrl" action="projectView">
            <ww:param name="projectId" value="${pageScope.project.id}"/>
          </ww:url>
          <ww:a href="%{projectViewUrl}">${pageScope.project.name}</ww:a>
        </ec:column>
        <ec:column property="editAction" title="&nbsp;" width="1%" sortable="false">
          <center>
            <pss:ifAuthorized permission="continuum-modify-group" resource="${projectGroup.name}">
            <c:choose>
              <c:when
                  test="${pageScope.project.state == 1 || pageScope.project.state == 10 || pageScope.project.state == 2 || pageScope.project.state == 3 || pageScope.project.state == 4}">
                <ww:url id="editProjectUrl" action="projectEdit">
                  <ww:param name="projectId" value="${pageScope.project.id}"/>
                  <ww:param name="projectName" value="${project.name}"/>
                </ww:url>
                <ww:a href="%{editProjectUrl}">
                  <img src="<ww:url value='/images/edit.gif'/>" alt="Edit" title="Edit" border="0">
                </ww:a>
              </c:when>
              <c:otherwise>
                <img src="<ww:url value='/images/edit_disabled.gif'/>" alt="Edit" title="Edit" border="0">
              </c:otherwise>
            </c:choose>
            </pss:ifAuthorized>
            <pss:elseAuthorized>
                <img src="<ww:url value='/images/edit_disabled.gif'/>" alt="Edit" title="Edit" border="0">
            </pss:elseAuthorized>
          </center>
        </ec:column>
        <ec:column property="deleteAction" title="&nbsp;" width="1%" sortable="false">
          <center>
            <pss:ifAuthorized permission="continuum-modify-group" resource="${projectGroup.name}">
            <c:choose>
              <c:when
                  test="${pageScope.project.state == 1 || pageScope.project.state == 10 || pageScope.project.state == 2 || pageScope.project.state == 3 || pageScope.project.state == 4}">
                <ww:url id="removeProjectUrl" action="deleteProject!default.action">
                  <ww:param name="projectId" value="${pageScope.project.id}"/>
                  <ww:param name="projectName" value="${pageScope.project.name}"/>
                </ww:url>
                <ww:a href="%{removeProjectUrl}">
                  <img src="<ww:url value='/images/delete.gif'/>" alt="Delete" title="Delete" border="0">
                </ww:a>
              </c:when>
              <c:otherwise>
                <img src="<ww:url value='/images/delete_disabled.gif'/>" alt="Delete" title="Delete" border="0">
              </c:otherwise>
            </c:choose>
            </pss:ifAuthorized>
            <pss:elseAuthorized>
                <img src="<ww:url value='/images/delete_disabled.gif'/>" alt="Delete" title="Delete" border="0">
            </pss:elseAuthorized>
          </center>
        </ec:column>
      </ec:row>
    </ec:table>
    
  <pss:ifAuthorized permission="continuum-manage-users">
  <h3>Users</h3>
    
  <ww:form action="projectGroupMembers" theme="xhtml" method="post">
    <ww:hidden name="ascending" />
    <ww:hidden name="projectGroupId" />
    <tr>
      <td nowrap="true">
        <table cellpadding="0" cellspacing="0">               
          <ww:select label="User search"
               list="criteria"
               name="filterProperty"
               value="filterProperty" />
        </table>
      </td>               
      <td>
        <table cellpadding="0" cellspacing="0">
          <ww:textfield name="filterKey" />
        </table>
      </td>  
      <td colspan="2" align="right">
        <table cellpadding="0" cellspacing="0">
          <ww:submit value="Search"/>
        </table>
      </td>
    </tr>             
  </ww:form>

  <hr/>
  
  <table class="securityTable" border="1" cellspacing="0" cellpadding="2" width="80%">
    <thead>
      <tr>
        <th nowrap="true">
          <ww:form id="sortlist" name="sortlist" action="projectGroupMembers" theme="xhtml" method="post">
            <ww:if test="${ascending}">
              <ww:a href="javascript: sortlist.submit()"><img src="<ww:url value='/images/icon_sortdown.gif'/>" title="<ww:text name='Sort descending'/>" border="0"></ww:a> Username
            </ww:if>
            <ww:else>
              <ww:a href="javascript: sortlist.submit()"><img src="<ww:url value='/images/icon_sortup.gif'/>" title="<ww:text name='Sort ascending'/>" border="0"></ww:a> Username
            </ww:else>
            <ww:hidden name="ascending" value="${!ascending}"/>
            <ww:hidden name="projectGroupId" />
            <ww:hidden name="filterProperty" />
            <ww:hidden name="filterKey" />
          </ww:form>
        </th>   
        <th>Full Name</th>
        <th>Administrator</th>
        <th>Developer</th>
        <th>User</th>
      </tr>
    </thead>
    <tbody>
      <ww:iterator value="projectGroupUsers">
        <tr>
          <td>
            <ww:property value="username"/>
          </td>
          <td>
            <ww:property value="userFullName"/>
          </td>
          <td>
            <ww:if test="${administrator}">
              <img src="<ww:url value='/images/icon_success_sml.gif'/>" border="0">
            </ww:if>
          </td>
          <td>
            <ww:if test="${developer}">
              <img src="<ww:url value='/images/icon_success_sml.gif'/>" border="0">
            </ww:if>
          </td>
          <td>
            <ww:if test="${user}">
              <img src="<ww:url value='/images/icon_success_sml.gif'/>" border="0">
            </ww:if>
          </td>
        </tr>
      </ww:iterator>
    </tbody>
  </table>
  </pss:ifAuthorized>
  
  </div>
  </body>
</ww:i18n>
</html>
