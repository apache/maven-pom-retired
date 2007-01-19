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

<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%@ taglib uri="continuum" prefix="c1" %>
<ww:i18n name="localization.Continuum">
<html>
    <head>
        <title><ww:text name="add.m1.project.page.title"/></title>
    </head>
    <body>
        <div class="app">
            <div id="axial" class="h3">
            <h3><ww:text name="add.m1.project.section.title"/></h3>
                <div class="axial">
                    <ww:form method="post" action="addMavenOneProject.action" name="addMavenOneProject" enctype="multipart/form-data">
                        <c:if test="${!empty actionErrors}">
                          <div class="errormessage">
                            <c:forEach items="${actionErrors}" var="actionError">
                              <p><ww:text name="${actionError}"/></p>
                            </c:forEach>
                          </div>
                        </c:if>
                        <table>
                          <tbody>
                            <ww:textfield label="%{getText('add.m1.project.m1PomUrl.label')}" name="m1PomUrl">
                                <ww:param name="desc">
                                <table cellspacing="0" cellpadding="0">
                                  <tbody>
                                    <tr>
                                      <td><ww:text name="add.m1.project.m1PomUrl.username.label"/>: </td>
                                      <td><input type="text" name="scmUsername" size="20" id="addMavenOneProject_scmUsername"/><td>
                                    </tr>  
                                    <tr>
                                      <td><ww:text name="add.m1.project.m1PomUrl.password.label"/>: </td>
                                      <td><input type="password" name="scmPassword" size="20" id="addMavenOneProject_scmPassword"/><td>
                                    </tr>  
                                  </tbody>
                                </table>  
                                  <p><ww:text name="add.m1.project.m1PomUrl.message"/></p>
                                </ww:param>
                            </ww:textfield>
                            <ww:label>
                              <ww:param name="after"><strong><ww:text name="or"/></strong></ww:param>
                            </ww:label>
                            <ww:file label="%{getText('add.m1.project.m1PomFile.label')}" name="m1PomFile">
                                <ww:param name="desc"><p><ww:text name="add.m1.project.m1PomFile.message"/></p></ww:param>
                            </ww:file>
                            <ww:if test="disableGroupSelection == true">
                              <ww:hidden name="selectedProjectGroup"/>
                              <ww:hidden name="disableGroupSelection"/>
                              <ww:textfield label="%{getText('add.m1.project.projectGroup')}" name="projectGroupName" disabled="true"/>
                            </ww:if>
                            <ww:else>
                              <ww:select label="%{getText('add.m1.project.projectGroup')}" name="selectedProjectGroup" list="projectGroups" listKey="id" listValue="name"/>
                            </ww:else>
                          </tbody>
                        </table>
                        <div class="functnbar3">
                          <c1:submitcancel value="%{getText('add')}" cancel="%{getText('cancel')}"/>
                        </div>
                  </ww:form>
                </div>
            </div>
        </div>
    </body>
</html>
</ww:i18n>
