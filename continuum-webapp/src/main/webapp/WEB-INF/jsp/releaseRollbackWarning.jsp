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
<html>
  <ww:i18n name="localization.Continuum">
    <head>
        <title><ww:text name="releaseProject.page.title"/></title>
    </head>
    <body>
      <h3>
        <ww:text name="releaseProject.section.title">
          <ww:param><ww:property value="projectName"/></ww:param>
        </ww:text>
      </h3>
      <p>This will revert all changes made by the previous release?</p>
      <table>
        <tr>
          <td>
            <ww:form action="releaseRollback" method="post">
              <ww:hidden name="projectId"/>
              <ww:hidden name="releaseId"/>
              <ww:submit value="Rollback Changes"/>
            </ww:form>
          </td>
          <td>
            <ww:form action="releaseInProgress" method="post">
              <ww:hidden name="projectId"/>
              <ww:hidden name="releaseId"/>
              <ww:submit value="Cancel"/>
            </ww:form>
          </td>
        </tr>
      </table>
    </body>
  </ww:i18n>
</html>
