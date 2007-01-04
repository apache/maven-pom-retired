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
    </head>
    <body>
      <h2>Perform Project Release</h2>
      <ww:form action="releasePerformFromScm" method="post" validate="true">
        <h3>Release Perform Parameters</h3>
        <ww:hidden name="projectId"/>
        <div class="axial">
          <table border="1" cellspacing="2" cellpadding="3" width="100%">
            <ww:textfield label="SCM Connection URL" name="scmUrl"/>
            <ww:textfield label="SCM Username" name="scmUsername"/>
            <ww:password label="SCM Password" name="scmPassword"/>
            <ww:textfield label="SCM Tag" name="scmTag"/>
            <c:if test="${!empty(scmTagBase)}">
              <ww:textfield label="SCM Tag Base" name="scmTagBase"/>
            </c:if>
            <ww:textfield label="Maven Arguments" name="goals" value="clean deploy"/>
            <ww:checkbox label="Use Release Profile" name="useReleaseProfile" value="true"/>
          </table>
        </div>
        <ww:submit/>
      </ww:form>
    </body>
  </ww:i18n>
</html>
