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
        <meta http-equiv="refresh" content="10;url=<ww:url includeParams="all" />"/>
    </head>
    <body>
      <h2><ww:text name="releaseInProgress.section.title"/></h2>
      <h3><ww:property value="name"/></h3>
      <div class="axial">
        <table width="100%">
          <tr>
            <th><ww:text name="releaseInProgress.status"/></th>
            <th width="100%"><ww:text name="releaseInProgress.phase"/></th>
          </tr>
          <ww:iterator value="listener.phases">
            <tr>
              <td>
              <ww:if test="listener.completedPhases.contains( top )">
                <img src="<ww:url value='/images/icon_success_sml.gif'/>"
                     alt="Done" title="Done" border="0">
              </ww:if>
              <ww:elseif test="listener.inProgress.equals( top )">
                <ww:if test="listener.error == null">
                  <img src="<ww:url value='/images/building.gif'/>"
                       alt="In Progress" title="In Progress" border="0">
                </ww:if>
                <ww:else>
                  <img src="<ww:url value='/images/icon_error_sml.gif'/>"
                       alt="Error" title="Error" border="0">
                </ww:else>
              </ww:elseif>
              <ww:else>
                <img src="<ww:url value='/images/inqueue.gif'/>"
                     alt="Queued" title="Queued" border="0">
              </ww:else>
              </td>
              <td><ww:property/></td>
            </tr>
          </ww:iterator>
        </table>
      </div>

      <ww:form action="releaseInProgress" method="get">
        <ww:hidden name="projectId"/>
        <ww:hidden name="releaseId"/>
        <ww:submit value="Refresh"/>
      </ww:form>
    </body>
  </ww:i18n>
</html>
