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
      <ww:form action="releaseInProgress" method="get">
        <ww:hidden name="projectId"/>
        <ww:hidden name="releaseId"/>
          <p>
            The release goal is currently initializing...
          </p>
          <p>
            Please wait while the server prepares your project for release.
          </p>
        <ww:submit value="Refresh"/>
      </ww:form>
    </body>
  </ww:i18n>
</html>
