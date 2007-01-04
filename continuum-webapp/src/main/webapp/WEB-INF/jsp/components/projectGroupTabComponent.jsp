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

<ww:i18n name="localization.Continuum">

  <div id="h3">
    <div>
      <p style="border-top: 1px solid transparent; border-bottom: 1px solid #DFDEDE;">
        <ww:url id="projectGroupSummaryUrl" action="projectGroupSummary" includeParams="none">
          <ww:param name="projectGroupId" value="projectGroupId"/>
        </ww:url>
        <ww:url id="projectGroupMembersUrl" action="projectGroupMembers" includeParams="none">
          <ww:param name="projectGroupId" value="projectGroupId"/>
        </ww:url>
        <ww:url id="projectGroupBuildDefinitionUrl" action="projectGroupBuildDefinition" includeParams="none">
          <ww:param name="projectGroupId" value="projectGroupId"/>
        </ww:url>
        <ww:url id="projectGroupNotifierUrl" action="projectGroupNotifier" includeParams="none">
          <ww:param name="projectGroupId" value="projectGroupId"/>
        </ww:url>

        <ww:set name="tabName" value="tabName"/>
        <c:choose>
            <c:when test="${tabName != 'Summary'}">
                <a style="border: 1px solid #DFDEDE; padding-left: 1em; padding-right: 1em; text-decoration: none;" href="${projectGroupSummaryUrl}"><ww:text name="projectGroup.tab.summary"/></a>
            </c:when>
            <c:otherwise>
                <b style="border: 1px solid #DFDEDE; padding-left: 1em; padding-right: 1em;"><ww:text name="projectGroup.tab.summary"/></b>
            </c:otherwise>
        </c:choose>

        <c:choose>
            <c:when test="${tabName != 'Members'}">
                <a style="border: 1px solid #DFDEDE; padding-left: 1em; padding-right: 1em; text-decoration: none;" href="${projectGroupMembersUrl}"><ww:text name="projectGroup.tab.members"/></a>
            </c:when>
            <c:otherwise>
                <b style="border: 1px solid #DFDEDE; padding-left: 1em; padding-right: 1em;"><ww:text name="projectGroup.tab.members"/></b>
            </c:otherwise>
        </c:choose>

        <c:choose>
            <c:when test="${tabName != 'BuildDefinitions'}">
                <a style="border: 1px solid #DFDEDE; padding-left: 1em; padding-right: 1em; text-decoration: none;" href="${projectGroupBuildDefinitionUrl}"><ww:text name="projectGroup.tab.buildDefinitions"/></a>
            </c:when>
            <c:otherwise>
                <b style="border: 1px solid #DFDEDE; padding-left: 1em; padding-right: 1em;"><ww:text name="projectGroup.tab.buildDefinitions"/></b>
            </c:otherwise>
        </c:choose>

        <c:choose>
            <c:when test="${tabName != 'Notifier'}">
                <a style="border: 1px solid #DFDEDE; padding-left: 1em; padding-right: 1em; text-decoration: none;" href="${projectGroupNotifierUrl}"><ww:text name="projectGroup.tab.notifiers"/></a>
            </c:when>
            <c:otherwise>
                <b style="border: 1px solid #DFDEDE; padding-left: 1em; padding-right: 1em;"><ww:text name="projectGroup.tab.notifiers"/></b>
            </c:otherwise>
        </c:choose>
      </p>
    </div>
  </div>
</ww:i18n>