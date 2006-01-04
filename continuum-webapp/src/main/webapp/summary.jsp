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
        <ww:set name="projects" value="projects" scope="request"/>
        <ec:table items="projects"
                  var="project"
                  showExports="false"
                  showPagination="false"
                  showStatusBar="false"
                  filterable="false">
          <ec:row highlightRow="true">
            <ec:column property="state" title="&nbsp;" cell="org.apache.maven.continuum.web.view.StateCell"/>
            <ec:column property="name">
                <a href="${pageContext.request.contextPath}/projectView.action?projectId=${pageScope.project.id}">${pageScope.project.name}</a>
            </ec:column>
            <ec:column property="version"/>
            <ec:column property="buildNumber" title="summary.projectTable.build" cell="org.apache.maven.continuum.web.view.BuildCell"/>
            <ec:column property="projectGroupName" title="summary.projectTable.group"/>
            <ec:column property="buildNowAction" title="&nbsp;" cell="org.apache.maven.continuum.web.view.BuildNowCell"/>
            <ec:column property="buildHistoryAction" title="&nbsp;">
                <a href="<ww:url value="/buildResults.action?projectId="/>${pageScope.project.id}&projectName=${pageScope.project.name}"><ww:text name="summary.buildHistory"/></a>
            </ec:column>
            <ec:column property="workingCopyAction" title="&nbsp;">
                <a href="<ww:url value="/workingCopy.action?projectId="/>${pageScope.project.id}&projectName=${pageScope.project.name}"><ww:text name="workingCopy"/></a>
            </ec:column>
            <ec:column property="deleteAction" title="&nbsp;">
                <a href="${pageContext.request.contextPath}/deleteProject!default.action?projectId=${pageScope.project.id}&projectName=${pageScope.project.name}"><ww:text name="delete"/></a>
            </ec:column>
          </ec:row>
        </ec:table>
        <div class="functnbar3">
          <form method="post" action="buildProject.action">
              <img src="<ww:url value="/images/icon_success_sml.gif"/>" alt="<ww:text name="message.success"/>" title="<ww:text name="message.success"/>"/> <ww:property value="nbSuccesses"/>
              <img src="<ww:url value="/images/icon_warning_sml.gif"/>" alt="<ww:text name="message.failed"/>" title="<ww:text name="message.failed"/>"/> <ww:property value="nbFailures"/>
              <img src="<ww:url value="/images/icon_error_sml.gif"/>" alt="<ww:text name="message.error"/>" title="<ww:text name="message.error"/>"/> <ww:property value="nbErrors"/>
              <ww:submit value="%{getText('summary.buildAll')}" theme="simple"/>
          </form>
        </div>
      </div>
    </body>
  </ww:i18n>
</html>