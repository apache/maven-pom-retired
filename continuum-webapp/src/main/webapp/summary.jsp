<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="/tld/extremecomponents" prefix="ec" %>
<html>
  <ww:i18n name="localization.Continuum">
    <head>
        <title><ww:text name="summary.page.title"/></title>
    </head>
    <body>
        <ec:table items="projects"
                  var="project"
                  showExports="false"
                  showPagination="false"
                  showStatusBar="false"
                  filterable="false">
          <ec:row highlightRow="true">
            <ec:column property="status" title="&nbsp;">
                &nbsp;
            </ec:column>
            <ec:column property="name"/>
            <ec:column property="version"/>
            <ec:column property="buildNumber" title="summary.projectTable.build">
                &nbsp;
            </ec:column>
            <ec:column property="groupName" title="summary.projectTable.group">
                ${pageScope.project.projectGroup.name}
            </ec:column>
          </ec:row>
        </ec:table>
      <div id="h3">
        <h3><ww:text name="summary.section.title"/></h3>
        <table border="1" cellspacing="2" cellpadding="3" width="100%" id="projectSummaryTable">
    
          <tbody><tr>
            <th>&nbsp;</th>
            <th width="100%"><ww:text name="summary.projectTable.name"/></th>
            <th><ww:text name="summary.projectTable.version"/></th>
            <th><ww:text name="summary.projectTable.build"/></th>
            <th><ww:text name="summary.projectTable.group"/></th>
            <th colspan="7"></th>
          </tr>
          <% String trStyle = "a"; %>
          <ww:iterator value="projects">
            <tr class="<%= "a".equals( trStyle ) ? "b" : "a" %>">
              <td>&nbsp;</td>
              <td><ww:property value="name"/></td>
              <td><ww:property value="version"/></td>
              <td>&nbsp;</td>
              <td><ww:property value="projectGroup.name"/></td>
              <td colspan="7">&nbsp;</td>
            </tr>
            <% trStyle = "a".equals( trStyle ) ? "b" : "a"; %>
          </ww:iterator>
          </tbody>
        </table>
        <div class="functnbar3">
          <img src="<ww:url value="/images/icon_success_sml.gif"/>" alt="<ww:text name="message.success"/>" title="<ww:text name="message.success"/>"/> <font color="red">TODO</font>
          <img src="<ww:url value="/images/icon_warning_sml.gif"/>" alt="<ww:text name="message.failed"/>" title="<ww:text name="message.failed"/>"/> <font color="red">TODO</font>
          <img src="<ww:url value="/images/icon_error_sml.gif"/>" alt="<ww:text name="message.error"/>" title="<ww:text name="message.error"/>"/> <font color="red">TODO</font>
        </div>
      </div>
    </body>
  </ww:i18n>
</html>