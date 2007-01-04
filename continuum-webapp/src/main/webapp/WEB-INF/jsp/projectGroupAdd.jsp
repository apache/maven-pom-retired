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
        <title><ww:text name="projectGroup.add.page.title"/></title>
    </head>
    <body>
      <div id="axial" class="h3">
        <h3><ww:text name="projectGroup.add.section.title"/></h3>

        <div class="axial">
          <ww:url id="actionUrl" action="addProjectGroup" includeContext="false" />
          <ww:form action="%{actionUrl}" method="post" >
            <c:if test="${!empty actionErrors}">
              <div class="errormessage">
                <c:forEach items="${actionErrors}" var="actionError">
                  <p><ww:text name="${actionError}"/></p>
                </c:forEach>
              </div>
            </c:if>
            <table>
              <tbody>
                <ww:textfield label="%{getText('projectGroup.name.label')}" name="name"  required="true"/>
                <ww:textfield label="%{getText('projectGroup.groupId.label')}" name="groupId" required="true"/>
                <ww:textfield label="%{getText('projectGroup.description.label')}" name="description"/>
              </tbody>
            </table>
            <div class="functnbar3">
              <c1:submitcancel value="%{getText('save')}" cancel="%{getText('cancel')}"/>
            </div>
          </ww:form>
        </div>
      </div>
    </body>
  </ww:i18n>
</html>
