<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="/tld/extremecomponents" prefix="ec" %>
<html>
  <ww:i18n name="localization.Continuum">
    <head>
        <title><ww:text name="summary.page.title"/></title>
    </head>
    <body>
      <div id="h3">
        <h3><ww:text name="summary.section.title"/></h3>
        <ec:table items="projects"
                  var="project"
                  showExports="false"
                  showPagination="false"
                  showStatusBar="false"
                  filterable="false">
          <ec:row highlightRow="true">
            <ec:column property="state" title="&nbsp;" cell="org.apache.maven.continuum.web.view.StateCell"/>
            <ec:column property="name">
                <a href="TO_BE_DEFINE">${pageScope.project.name}</a>
            </ec:column>
            <ec:column property="version"/>
            <ec:column property="buildNumber" title="summary.projectTable.build"  cell="org.apache.maven.continuum.web.view.BuildCell"/>
            <ec:column property="groupName" title="summary.projectTable.group">
                ${pageScope.project.projectGroupName}
            </ec:column>
          </ec:row>
        </ec:table>
        <div class="functnbar3">
          <img src="<ww:url value="/images/icon_success_sml.gif"/>" alt="<ww:text name="message.success"/>" title="<ww:text name="message.success"/>"/> <font color="red">TODO</font>
          <img src="<ww:url value="/images/icon_warning_sml.gif"/>" alt="<ww:text name="message.failed"/>" title="<ww:text name="message.failed"/>"/> <font color="red">TODO</font>
          <img src="<ww:url value="/images/icon_error_sml.gif"/>" alt="<ww:text name="message.error"/>" title="<ww:text name="message.error"/>"/> <font color="red">TODO</font>
        </div>
      </div>
    </body>
  </ww:i18n>
</html>