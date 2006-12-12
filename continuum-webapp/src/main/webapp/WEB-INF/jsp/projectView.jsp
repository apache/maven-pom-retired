<%@ taglib uri="/webwork" prefix="ww" %>
<%@ taglib uri="http://www.extremecomponents.org" prefix="ec" %>
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<%@ taglib uri="continuum" prefix="c1" %>
<%@ taglib uri="/plexusSecuritySystem" prefix="pss" %>

<html>
  <ww:i18n name="localization.Continuum">
    <head>
        <title><ww:text name="projectView.page.title"/></title>
    </head>
    <body>
      <div id="h3">

        <jsp:include page="/WEB-INF/jsp/navigations/ProjectMenu.jsp">
          <jsp:param name="tab" value="view"/>
          <jsp:param name="projectGroupId" value="%{project.projectGroup.id}"/>
        </jsp:include>

        <h3><ww:text name="projectView.section.title"/></h3>

        <div class="axial">
          <table border="1" cellspacing="2" cellpadding="3" width="100%">
            <c1:data label="%{getText('projectView.project.name')}" name="project.name"/>
            <c1:data label="%{getText('projectView.project.version')}" name="project.version"/>
            <c1:data label="%{getText('projectView.project.scmUrl')}" name="project.scmUrl"/>
            <ww:url id="projectGroupSummaryUrl" value="/projectGroupSummary.action">
                <ww:param name="projectGroupId" value="%{project.projectGroup.id}"/>
            </ww:url>            
            <c1:data label="%{getText('projectView.project.group')}" name="project.projectGroup.name" valueLink="%{'${projectGroupSummaryUrl}'}"/>
          </table>

          <pss:ifAuthorized permission="continuum-modify-group" resource="${project.projectGroup.name}">
          <div class="functnbar3">
            <table>
              <tbody>
              <tr>
                <td>
                  <form action="projectEdit.action" method="post">
                    <input type="hidden" name="projectId" value="<ww:property value="project.id"/>"/>
                    <input type="submit" name="edit-project" value="<ww:text name="edit"/>"/>
                  </form>
                </td>
                <td>
                  <form method="post" action="buildProject.action">
                    <input type="hidden" name="projectId" value="<ww:property value="project.id"/>"/>
                    <input type="submit" name="build-project" value="<ww:text name="summary.buildNow"/>"/>
                  </form>
                </td>
              </tr>
              </tbody>
            </table>
          </div>
          </pss:ifAuthorized>
        </div>

        <h3><ww:text name="projectView.buildDefinitions"/></h3>

        <ww:action name="buildDefinitionSummary" id="summary" namespace="component" executeResult="true">
          <ww:param name="projectId" value="%{project.id}" />
        </ww:action>

        <div class="functnbar3">
           <pss:ifAuthorized permission="continuum-modify-group" resource="${project.projectGroup.name}">
          <ww:form action="buildDefinition" method="post">
            <input type="hidden" name="projectId" value="<ww:property value="project.id"/>"/>
            <ww:submit value="%{getText('add')}"/>
          </ww:form>
          </pss:ifAuthorized>
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
               <pss:ifAuthorized permission="continuum-modify-group" resource="${project.projectGroup.name}">
                <c:if test="${!pageScope.notifier.fromProject}">
                    <a href='<ww:url value="${notifier.type}ProjectNotifierEdit!default.action">
                      <ww:param name="notifierId" value="${notifier.id}"/>
                      <ww:param name="projectId" value="project.id"/>
                      <ww:param name="projectGroupId" value="${project.projectGroup.id}"/>
                      <ww:param name="notifierType" value="${notifier.type}"/>
                      </ww:url>'>
                      <img src="<ww:url value='/images/edit.gif'/>" alt="<ww:text name='edit'/>" title="<ww:text name='edit'/>" border="0" />
                    </a>
                    &nbsp;
                    <a href='<ww:url value="/deleteProjectNotifier!default.action">
                      <ww:param name="projectId" value="project.id"/>
                      <ww:param name="projectGroupId" value="${project.projectGroup.id}"/>
                      <ww:param name="notifierType" value="${notifier.type}"/>
                      <ww:param name="notifierId" value="${notifier.id}"/>
                      </ww:url>'>
                      <img src="<ww:url value='/images/delete.gif'/>" alt="<ww:text name='delete'/>" title="<ww:text name='delete'/>" border="0">
                    </a>
                </c:if>
              </pss:ifAuthorized>
            </ec:column>
          </ec:row>
        </ec:table>
        <div class="functnbar3">
           <pss:ifAuthorized permission="continuum-modify-group" resource="${project.projectGroup.name}">
          <ww:form action="addProjectNotifier!default.action" method="post">
            <input type="hidden" name="projectId" value="<ww:property value="project.id"/>"/>
            <input type="hidden" name="projectGroupId" value="<ww:property value="project.projectGroup.id"/>"/>
            <ww:submit value="%{getText('add')}"/>
          </ww:form>
          </pss:ifAuthorized>
        </div>

        <h3><ww:text name="projectView.dependencies"/></h3>
        <ww:set name="dependencies" value="project.dependencies" scope="request"/>
        <ec:table items="dependencies"
                  var="dep"
                  showExports="false"
                  showPagination="false"
                  showStatusBar="false"
                  filterable="false"
                  sortable="false">
          <ec:row>
            <ec:column property="groupId" title="projectView.dependency.groupId"/>
            <ec:column property="artifactId" title="projectView.dependency.artifactId"/>
            <ec:column property="version" title="projectView.dependency.version"/>
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
