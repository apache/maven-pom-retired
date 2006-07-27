<%@ taglib uri="/webwork" prefix="ww" %>
<%@ taglib uri="/tld/extremecomponents" prefix="ec" %>
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
                  filterable="false">
         <ec:row>
            <ec:column property="name" title="schedules.table.name"/>
            <ec:column property="description" title="schedules.table.description"/>
            <ec:column property="delay" title="schedules.table.delay"/>
            <ec:column property="cronExpression" title="schedules.table.cronExpression"/>
            <ec:column property="actions" title="&nbsp;">
                <c:url var="editScheduleUrl" value="/editSchedule!edit.action">
                  <c:param name="id" value="${schedule.id}"/>
                </c:url>
                <a href="<c:out value='${editScheduleUrl}'/>"><ww:text name="edit"/></a>
                &nbsp;
                <ww:text name="delete"/>
            </ec:column>
          </ec:row>
        </ec:table>
      </div>
      <div class="functnbar3">
        <ww:form action="addSchedule!default.action" method="post">
          <ww:submit value="%{getText('add')}"/>
        </ww:form>
    </div>
    </body>
  </ww:i18n>
</html>
