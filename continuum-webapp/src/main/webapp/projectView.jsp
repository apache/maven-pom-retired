<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="/tld/extremecomponents" prefix="ec" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<html>
  <ww:i18n name="localization.Continuum">
    <head>
        <title><ww:text name="projectView.page.title"/></title>
    </head>
    <body>
      <div id="h3">
        <div>
          <p style="border-top: 1px solid transparent; border-bottom: 1px solid black;">
            <a style="border: 1px solid black; padding-left: 1em; padding-right: 1em;">Info</a>
            <a style="border: 1px solid black; padding-left: 1em; padding-right: 1em; text-decoration:none;" href="<ww:url value="/buildResults.action?projectId="/><ww:property value="project.id"/>&projectName=<ww:property value="project.name"/>">Builds</a>
            <a style="border: 1px solid black; padding-left: 1em; padding-right: 1em; text-decoration:none;" href="<ww:url value="/workingCopy.action?projectId="/><ww:property value="project.id"/>&projectName=<ww:property value="project.name"/>">Working&nbsp;Copy</a>
          </p>
        </div>

        <h3><ww:text name="projectView.section.title"/></h3>
            
        <div class="axial">
          <table border="1" cellspacing="2" cellpadding="3" width="100%">
            <tr class="b">
              <th><ww:text name="projectView.project.name"/></th>
              <td><ww:property value="project.name"/></td>
            </tr>
            <tr class="b">
              <th><ww:text name="projectView.project.version"/></th>
              <td><ww:property value="project.version"/></td>
            </tr>
            <tr class="b">
              <th><ww:text name="projectView.project.scmUrl"/></th>
              <td><ww:property value="project.scmUrl"/></td>
            </tr>
            <tr class="b">
              <th><ww:text name="projectView.project.group"/></th>
              <td><ww:property value="project.projectGroup.name"/></td>
            </tr>
          </table>
          <div class="functnbar3">
            <ww:form action="projectEdit!edit.action">
                <input type="hidden" name="projectId" value="<ww:property value="project.id"/>"/>
                <ww:submit value="%{getText('edit')}"/>
            </ww:form>
          </div>
        </div>

        <h3><ww:text name="projectView.buildDefinitions"/></h3>
        <ww:set name="buildDefinitions" value="project.buildDefinitions" scope="request"/>
        <ec:table items="buildDefinitions"
                  var="buildDefinition"
                  showExports="false"
                  showPagination="false"
                  showStatusBar="false"
                  filterable="false"
                  sortable="false">
          <ec:row>
            <ec:column property="goals" title="projectView.buildDefinition.goals"/>
            <ec:column property="arguments" title="projectView.buildDefinition.arguments"/>
            <ec:column property="buildFile" title="projectView.buildDefinition.buildFile"/>
            <ec:column property="profile" title="projectView.buildDefinition.profile"/>
            <ec:column property="schedule" title="schedule">
                ${pageScope.buildDefinition.schedule.name}
            </ec:column>
            <ec:column property="from" title="projectView.buildDefinition.from">
                PROJECT
            </ec:column>
            <ec:column property="actions" title="&nbsp;">
                <a href="${pageContext.request.contextPath}/buildDefinitionEdit!default.action?projectId=<ww:property value="project.id"/>&buildDefinitionId=${pageScope.buildDefinition.id}"><ww:text name="edit"/></a>
                &nbsp;
                <a href="${pageContext.request.contextPath}/deleteBuildDefinition!default.action?projectId=<ww:property value="project.id"/>&buildDefinitionId=${pageScope.buildDefinition.id}"><ww:text name="delete"/></a>
            </ec:column>
          </ec:row>
        </ec:table>
        <div class="functnbar3">
          <form method="post" action="addBuildDefinition.action">
              <input type="hidden" name="projectId" value="<ww:property value="project.id"/>"/>
              <input type="submit" name="addBuildDef" value="<ww:text name="add"/>"/>
          </form>
        </div>

        <h3><ww:text name="projectView.notifiers"/></h3>
        <ww:set name="notifiers" value="project.notifiers" scope="request"/>
        <ec:table items="notifiers"
                  var="notifier"
                  showExports="false"
                  showPagination="false"
                  showStatusBar="false"
                  filterable="false"
                  sortable="false">
          <ec:row>
            <ec:column property="type" title="projectView.notifier.type"/>
            <ec:column property="recipient" title="projectView.notifier.recipient" cell="org.apache.maven.continuum.web.view.projectview.NotifierRecipientCell"/>
            <ec:column property="events" title="projectView.notifier.events" cell="org.apache.maven.continuum.web.view.projectview.NotifierEventCell"/>
            <ec:column property="from" title="projectView.notifier.from" cell="org.apache.maven.continuum.web.view.projectview.NotifierFromCell"/>
            <ec:column property="actions" title="&nbsp;">
                <c:if test="${!pageScope.notifier.fromProject}">
                    <a href="${pageContext.request.contextPath}/${pageScope.notifier.type}NotifierEdit!default.action?projectId=<ww:property value="project.id"/>&notifierId=${pageScope.notifier.id}"><ww:text name="edit"/></a>
                    &nbsp;
                    <a href="${pageContext.request.contextPath}/deleteNotifier!default.action?projectId=<ww:property value="project.id"/>&notifierId=${pageScope.notifier.id}&notifierType=${pageScope.notifier.type}"><ww:text name="delete"/></a>
                </c:if>
            </ec:column>
          </ec:row>
        </ec:table>
        <div class="functnbar3">
          <form method="post" action="addNotifier!default.action">
              <input type="hidden" name="projectId" value="<ww:property value="project.id"/>"/>
              <input type="submit" name="addNotifier" value="<ww:text name="add"/>"/>
          </form>
        </div>

        <h3><ww:text name="projectView.dependencies"/></h3>
        <ww:set name="dependencies" value="project.dependencies" scope="request"/>
        <ec:table items="dependencies"
                  var="dependency"
                  showExports="false"
                  showPagination="false"
                  showStatusBar="false"
                  filterable="false"
                  sortable="false">
          <ec:row>
            <ec:column property="name" title="projectView.dependency.name">
                ${pageScope.dependency.groupId}:${pageScope.dependency.artifactId}:${pageScope.dependency.version}
            </ec:column>
          </ec:row>
        </ec:table>

        <h3><ww:text name="projectView.developers"/></h3>
        <ww:set name="developers" value="project.developers" scope="request"/>
        <ec:table items="developers"
                  showExports="false"
                  showPagination="false"
                  showStatusBar="false"
                  filterable="false"
                  sortable="false">
          <ec:row>
            <ec:column property="name" title="projectView.developer.name"/>
            <ec:column property="email" title="projectView.developer.email"/>
          </ec:row>
        </ec:table>

      </div>
    </body>
  </ww:i18n>
</html>
