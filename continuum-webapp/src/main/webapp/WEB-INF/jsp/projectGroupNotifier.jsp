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
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c' %>
<%@ taglib uri="continuum" prefix="c1" %>
<html>
  <ww:i18n name="localization.Continuum">
    <head>
      <title><ww:text name="projectGroup.page.title"/></title>
    </head>

    <body>
      <div id="h3">

        <ww:action name="projectGroupTab" executeResult="true">
          <ww:param name="tabName" value="'Notifier'"/>
        </ww:action>
    
        <ww:action name="projectGroupNotifierSummary" executeResult="true" namespace="component">
          <ww:param name="projectGroupId" value="%{projectGroupId}"/>
          <ww:param name="projectGroupName" value="%{projectGroup.name}"/>
        </ww:action>
      </div>
    </body>
  </ww:i18n>
</html>
