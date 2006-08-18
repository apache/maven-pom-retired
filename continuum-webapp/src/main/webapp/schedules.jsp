<%@ taglib uri="/webwork" prefix="ww" %>
<%@ taglib uri="/tld/extremecomponents" prefix="ec" %>
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
                  filterable="false">
         <ec:row>
            <ec:column property="name" title="schedules.table.name"/>
            <ec:column property="description" title="schedules.table.description"/>
            <ec:column property="delay" title="schedules.table.delay"/>
            <ec:column property="cronExpression" title="schedules.table.cronExpression"/>
            <ec:column property="actions" title="&nbsp;">
                <ww:url id="editUrl" action="schedule">
                  <ww:param name="id" value="${pageScope.schedule.id}"/>
                </ww:url>
                <ww:a href="%{editUrl}">Edit</ww:a>
                &nbsp;
                <ww:text name="delete"/>
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
