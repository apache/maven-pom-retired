<%@ taglib uri="webwork" prefix="ww" %>
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
            <ec:column property="deleteAction" title="&nbsp;">
                <a href="${pageContext.request.contextPath}/editSchedule!edit.action?id=${pageScope.schedule.id}">edit</a>
            </ec:column>
          </ec:row>
        </ec:table>
      </div>
      <table>
        <tr>
          <td>
            <form method="post" action="addSchedule!default.action">
              <input type="submit" name="add-build-def" value="Add"/>
            </form>
          </td>
        </tr>
      </table>
    </body>
  </ww:i18n>
</html>