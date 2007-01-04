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
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
  <ww:i18n name="localization.Continuum">
    <head>
      <title><ww:text name="releaseProject.page.title"/></title>
      <ww:head />
    </head>
    <body>
      <h2><ww:text name="releasePrepare.section.title"/></h2>
      <ww:form action="releasePrepare" method="post">
        <h3><ww:text name="releasePrepare.parameters"/></h3>
        <input type="hidden" name="projectId" value="<ww:property value="projectId"/>"/>
        <div class="axial">
          <table border="1" cellspacing="2" cellpadding="3" width="100%">
            <ww:textfield label="SCM Username" name="scmUsername" required="true"/>
            <ww:password label="SCM Password" name="scmPassword" required="true"/>
            <ww:textfield label="SCM Tag" name="scmTag" required="true"/>
            <c:if test="${!empty(scmTagBase)}">
              <ww:textfield label="SCM Tag Base" name="scmTagBase"/>
            </c:if>
            <ww:textfield label="Preparation Goals" name="prepareGoals" required="true"/>
           </table>
        </div>

        <ww:iterator value="projects">
          <h3><ww:property value="name"/></h3>
          <input type="hidden" name="projectKeys" value="<ww:property value="key"/>">
          <div class="axial">
            <table border="1" cellspacing="2" cellpadding="3" width="100%">
              <tr>
                <th><ww:text name="releasePrepare.releaseVersion"/></th>
                <td>
                  <input type=text name="relVersions"
                         value="<ww:property value="release"/>" size="100">
                </td>
              </tr>
              <tr>
                <th><ww:text name="releasePrepare.nextDevelopmentVersion"/></th>
                <td>
                  <input type=text name="devVersions"
                         value="<ww:property value="dev"/>" size="100">
                </td>
              </tr>
             </table>
           </div>
        </ww:iterator>

        <ww:submit/>
      </ww:form>
    </body>
  </ww:i18n>
</html>
