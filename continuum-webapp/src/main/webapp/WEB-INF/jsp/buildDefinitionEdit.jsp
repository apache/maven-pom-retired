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
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<%@ taglib uri="continuum" prefix="c1" %>
<html>
  <ww:i18n name="localization.Continuum">
    <head>
        <title><ww:text name="buildDefinition.page.title"/></title>
    </head>
    <body>
      <div id="axial" class="h3">
        <h3><ww:text name="buildDefinition.section.title"/></h3>

        <div class="axial">
          <ww:form action="saveBuildDefinition" method="post" validate="true">
            <c:choose>
            
              <c:when test="${!empty actionErrors}">
                <div class="errormessage">
                  <c:forEach items="${actionErrors}" var="actionError">
                    <p><ww:text name="${actionError}"/></p>
                  </c:forEach>
                </div>
                <input type="button" value="Back" onClick="history.go(-1)">
              </c:when>
  
              <c:when test="${empty actionErrors}">
                <table>
                  <tbody>
                    <ww:if test="executor == 'ant'">
                      <ww:textfield label="%{getText('buildDefinition.buildFile.ant.label')}" name="buildFile"  required="true"/>
                    </ww:if>
                    <ww:elseif test="executor == 'shell'">
                      <ww:textfield label="%{getText('buildDefinition.buildFile.shell.label')}" name="buildFile" required="true"/>
                    </ww:elseif>
                    <ww:else>
                      <ww:textfield label="%{getText('buildDefinition.buildFile.maven.label')}" name="buildFile" required="true"/>
                    </ww:else>
    
                    <ww:if test="executor == 'ant'">
                      <ww:textfield label="%{getText('buildDefinition.goals.ant.label')}" name="goals"/>
                    </ww:if>
                    <ww:elseif test="executor == 'shell'">
                    </ww:elseif>
                    <ww:else>
                      <ww:textfield label="%{getText('buildDefinition.goals.maven.label')}" name="goals"/>
                    </ww:else>
    
                    <ww:textfield label="%{getText('buildDefinition.arguments.label')}" name="arguments"/>
                    <ww:checkbox label="%{getText('buildDefinition.buildFresh.label')}" name="buildFresh" value="buildFresh" fieldValue="true"/>
                    <ww:if test="defaultBuildDefinition == true">
                      <ww:label label="%{getText('buildDefinition.defaultForProject.label')}" value="true"/>
                    </ww:if>
                    <ww:else>
                      <ww:checkbox label="%{getText('buildDefinition.defaultForProject.label')}"  name="defaultBuildDefinition" value="defaultBuildDefinition" fieldValue="true"/>
                    </ww:else>
                    <ww:select label="%{getText('buildDefinition.schedule.label')}" name="scheduleId" list="schedules"/>
                  </tbody>
                </table>
                <div class="functnbar3">
                  <c1:submitcancel value="%{getText('save')}" cancel="%{getText('cancel')}"/>
                </div>

                <ww:hidden name="buildDefinitionId"/>
                <ww:hidden name="projectId"/>
                <ww:hidden name="projectGroupId"/>
                <ww:hidden name="groupBuildDefinition"/>
                <ww:if test="defaultBuildDefinition == true">
                  <ww:hidden name="defaultBuildDefinition" value="true"/>
                </ww:if>
              </c:when>
            
            </c:choose>
          </ww:form>
        </div>
      </div>
    </body>
  </ww:i18n>
</html>
