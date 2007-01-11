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
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<%@ taglib uri="continuum" prefix="c1" %>
<%@ taglib uri="/plexusSecuritySystem" prefix="pss" %>
<ww:i18n name="localization.Continuum">

  <h3>Project Group Notifiers of ${projectGroup.name} group</h3>
  <ww:if test="${not empty projectGroupNotifierSummaries}">
  <ec:table items="projectGroupNotifierSummaries"
            var="projectGroupNotifierSummary"
            showExports="false"
            showPagination="false"
            showStatusBar="false"
            filterable="false"
            sortable="false">
    <ec:row>
      <ec:column property="type" title="projectView.notifier.type"/>
      <ec:column property="recipient" title="projectView.notifier.recipient"/>
      <ec:column property="events" title="projectView.notifier.events"/>
      <ec:column property="sender" title="projectView.notifier.from"/>
      <ec:column property="editActions" title="&nbsp;" width="1%">
        <ww:if test="${pageScope.projectGroupNotifierSummary.from == 'PROJECT'}">
          <img src="<ww:url value='/images/edit_disabled.gif'/>" alt="Edit" title="Edit" border="0">
        </ww:if>
        <ww:else>
          <pss:ifAuthorized permission="continuum-modify-group" resource="${projectGroupName}">
            <ww:url id="editUrl" action="editProjectGroupNotifier" namespace="/">
              <ww:param name="projectGroupId">${pageScope.projectGroupNotifierSummary.projectGroupId}</ww:param>
              <ww:param name="notifierId">${pageScope.projectGroupNotifierSummary.id}</ww:param>
              <ww:param name="notifierType">${pageScope.projectGroupNotifierSummary.type}</ww:param>
            </ww:url>
            <ww:a href="%{editUrl}">
              <img src="<ww:url value='/images/edit.gif'/>" alt="Edit" title="Edit" border="0">
            </ww:a>
          </pss:ifAuthorized>
          <pss:elseAuthorized>
            <img src="<ww:url value='/images/edit_disabled.gif'/>" alt="Edit" title="Edit" border="0">
          </pss:elseAuthorized>
        </ww:else>
      </ec:column>    
      <ec:column property="deleteActions" title="&nbsp;" width="1%">
        <ww:if test="${pageScope.projectGroupNotifierSummary.from == 'PROJECT'}">
          <img src="<ww:url value='/images/delete_disabled.gif'/>" alt="Edit" title="Edit" border="0">
        </ww:if>
        <ww:else>
          <pss:ifAuthorized permission="continuum-modify-group" resource="${projectGroupName}">
            <ww:url id="removeUrl" action="deleteProjectGroupNotifier!default.action" namespace="/">
              <ww:param name="projectGroupId">${pageScope.projectGroupNotifierSummary.projectGroupId}</ww:param>
              <ww:param name="notifierId">${pageScope.projectGroupNotifierSummary.id}</ww:param>
              <ww:param name="notifierType">${pageScope.projectGroupNotifierSummary.type}</ww:param>
              <ww:param name="confirmed" value="false"/>
            </ww:url>
          <ww:a href="%{removeUrl}">
            <img src="<ww:url value='/images/delete.gif'/>" alt="Delete" title="Delete" border="0">
          </ww:a>
          </pss:ifAuthorized>
          <pss:elseAuthorized>
            <img src="<ww:url value='/images/delete_disabled.gif'/>" alt="Delete" title="Delete" border="0">
          </pss:elseAuthorized>
        </ww:else>
      </ec:column>      
    </ec:row>
  </ec:table>
  </ww:if>

  <pss:ifAuthorized permission="continuum-modify-group" resource="${projectGroupName}">
    <div class="functnbar3">
      <ww:url id="addUrl" action="addProjectGroupNotifier" namespace="/"  includeContext="false" includeParams="none" />
      <ww:form action="%{addUrl}" method="post">
        <input type="hidden" name="projectGroupId" value="<ww:property value="projectGroupId"/>"/>
        <ww:submit value="%{getText('add')}"/>
        </ww:form>
    </div>
  </pss:ifAuthorized>

  <ww:if test="${not empty projectNotifierSummaries}">
    <h3>Project Notifiers</h3>
    <ec:table items="projectNotifierSummaries"
              var="projectNotifierSummary"
              showExports="false"
              showPagination="false"
              showStatusBar="false"
              filterable="false"
              sortable="false">
      <ec:row>
        <ec:column property="type" title="projectView.notifier.type"/>
        <ec:column property="recipient" title="projectView.notifier.recipient"/>
        <ec:column property="events" title="projectView.notifier.events"/>
        <ec:column property="sender" title="projectView.notifier.from"/>
        <ec:column property="state" value="Enabled/Disabled" />
      </ec:row>
    </ec:table>
  </ww:if>
</ww:i18n>
