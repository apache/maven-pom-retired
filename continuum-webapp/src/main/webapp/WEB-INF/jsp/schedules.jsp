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
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<html>
  <ww:i18n name="localization.Continuum">
    <head>
        <title><ww:text name="schedules.page.title"/></title>
    </head>
    <body>
      <div id="h3">
        <h3><ww:text name="schedules.section.title"/></h3>
        <ww:set name="schedules" value="schedules" scope="request"/>
        <ec:table items="schedules"
                  var="schedule"
                  showExports="false"
                  showPagination="false"
                  showStatusBar="false"
                  sortable="false"
                  filterable="false">
         <ec:row>
            <ec:column property="name" title="schedules.table.name"/>
            <ec:column property="description" title="schedules.table.description"/>
            <ec:column property="delay" title="schedules.table.delay"/>
            <ec:column property="cronExpression" title="schedules.table.cronExpression"/>
            <ec:column property="maxJobExecutionTime" title="schedules.table.maxJobExecutionTime"/>
            <ec:column property="active" title="schedules.table.active"/>
            <ec:column property="editActions" title="&nbsp;" width="1%">
                <ww:url id="editScheduleUrl" action="schedule">
                  <ww:param name="id" value="${pageScope.schedule.id}"/>
                </ww:url>
                <ww:a href="%{editScheduleUrl}"><img src="<ww:url value='/images/edit.gif'/>" alt="<ww:text name='edit'/>" title="<ww:text name='edit'/>" border="0" /></ww:a>
            </ec:column>
            <ec:column property="editActions" title="&nbsp;" width="1%">
                <ww:url id="removeScheduleUrl" action="removeSchedule">
                  <ww:param name="id" value="${pageScope.schedule.id}"/>
                  <ww:param name="name" value="%{'${pageScope.schedule.name}'}"/>                  
                </ww:url>
                <ww:a href="%{removeScheduleUrl}"><img src="<ww:url value='/images/delete.gif'/>" alt="<ww:text name='delete'/>" title="<ww:text name='delete'/>" border="0"></ww:a>
            </ec:column>
          </ec:row>
        </ec:table>
      </div>
      <div class="functnbar3">
        <ww:form action="schedule" method="post">
          <ww:submit value="%{getText('add')}"/>
        </ww:form>
    </div>
    </body>
  </ww:i18n>
</html>
