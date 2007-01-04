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
<%@ taglib uri="http://www.extremecomponents.org" prefix="ec" %>
<html>
  <ww:i18n name="localization.Continuum">
    <head>
        <title><ww:text name="projectGroup.edit.page.title"/></title>
    </head>
    <body>
      <div id="axial" class="h3">
        <h3><ww:text name="projectGroup.edit.section.title"/></h3>

        <div class="axial">
          <ww:form action="saveProjectGroup" method="post" validate="true">
              <c:if test="${projectInCOQueue}">
                <div class="label">
                    <p><ww:text name="%{getText('project.in.checkout.queue.error')}"/></p>
                            </div >
              </c:if>
            <table>
              <tbody>
                <ww:hidden name="projectGroupId"/>
                <ww:textfield label="%{getText('projectGroup.name.label')}" name="name" required="true" disabled="%{projectInCOQueue}"/>
                <c1:data label="%{getText('projectGroup.groupId.label')}" name="projectGroup.groupId"/>
                <ww:textfield label="%{getText('projectGroup.description.label')}" name="description" disabled="%{projectInCOQueue}"/>
              </tbody>
            </table>
            
            <h3>Projects</h3>
            <div class="eXtremeTable">
              <table id="projects_table" border="1" cellspacing="2" cellpadding="3" class="tableRegion" width="100%">
                <thead>
                  <tr>
                    <td class="tableHeader"><ww:text name="Project Name"/></td>
                    <td class="tableHeader"><ww:text name="Move to Group"/></td>
                  </tr>
                </thead>
                <tbody class="tableBody">
                  <ww:iterator value="projectList" status="rowCounter">
                    <tr class="<ww:if test="#rowCounter.odd == true">odd</ww:if><ww:else>even</ww:else>">
                      <td><ww:select cssStyle="width:200px" label="%{name}" name="projects[%{id}]" list="projectGroups" value="%{projectGroup.id}" disabled="%{projectInCOQueue}"/></td>
                    </tr>
                  </ww:iterator>
                </tbody>
              </table>
            </div>
            <div class="functnbar3">
              <c:choose>
                <c:when test="${!projectInCOQueue}">
                  <c1:submitcancel value="%{getText('save')}" cancel="%{getText('cancel')}"/>
                </c:when>
                <c:otherwise>
                  <input type="button" value="Back" onClick="history.go(-1)">
                </c:otherwise>
              </c:choose>
            </div>
          </ww:form>
        </div>
      </div>
    </body>
  </ww:i18n>
</html>
