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
<html>
  <ww:i18n name="localization.Continuum">
<head>
<title><ww:text name="editSchedule.page.title"/></title>
</head>
<body>
<div class="app">
  <div id="axial" class="h3">
    <h3><ww:text name="editSchedule.page.title"/></h3>

    <div class="axial">
      <ww:form action="saveSchedule" method="post" validate="true">
        <c:if test="${!empty actionErrors}">
          <div class="errormessage">
            <c:forEach items="${actionErrors}" var="actionError">
              <p><ww:text name="${actionError}"/></p>
            </c:forEach>
          </div>
        </c:if>
        <ww:hidden name="id"/>
          <table>
            <ww:textfield label="%{getText('schedule.name.label')}" name="name" required="true">
                <ww:param name="desc"><p><ww:text name="schedule.name.message"/></p></ww:param>
            </ww:textfield>
            <ww:textfield label="%{getText('schedule.description.label')}" name="description" required="true">
                <ww:param name="desc"><p><ww:text name="schedule.description.message"/></p></ww:param>
            </ww:textfield>

            <tr>
              <th><ww:label theme="simple" value="%{getText('schedule.cronExpression.label')}:"/></th>
              <td>
                <table>
                  <ww:textfield label="%{getText('schedule.second.label')}" name="second" size="2"/>
                  <ww:textfield label="%{getText('schedule.minute.label')}" name="minute" size="2"/>
                  <ww:textfield label="%{getText('schedule.hour.label')}" name="hour"  size="2"/>
                  <ww:textfield label="%{getText('schedule.dayOfMonth.label')}" name="dayOfMonth"  size="2"/>
                  <ww:textfield label="%{getText('schedule.month.label')}" name="month"  size="2"/>
                  <ww:textfield label="%{getText('schedule.dayOfWeek.label')}" name="dayOfWeek"  size="2"/>
                  <ww:textfield label="%{getText('schedule.year.label')}" name="year"  size="4">
                    <ww:param name="desc"><p><ww:text name="schedule.cronExpression.message"/></p></ww:param>
                  </ww:textfield>
                </table>
              </td>
            </tr>

            <ww:textfield label="%{getText('schedule.maxJobExecutionTime.label')}" name="maxJobExecutionTime" required="true">
                <ww:param name="desc"><p><ww:text name="schedule.maxJobExecutionTime.message"/></p></ww:param>
            </ww:textfield>
            <ww:textfield label="%{getText('schedule.quietPeriod.label')}" name="delay">
                <ww:param name="desc"><p><ww:text name="schedule.quietPeriod.message"/></p></ww:param>
            </ww:textfield>
            <ww:checkbox label="%{getText('schedule.enabled.label')}" name="active" value="active" fieldValue="true">
                <ww:param name="desc"><p><ww:text name="schedule.enabled.message"/></p></ww:param>
            </ww:checkbox>
          </table>

        <div class="functnbar3">
          <c1:submitcancel value="%{getText('save')}" cancel="%{getText('cancel')}"/>
        </div>
      </ww:form>
    </div>
  </div>
</div>

</body>
</ww:i18n>
</html>
