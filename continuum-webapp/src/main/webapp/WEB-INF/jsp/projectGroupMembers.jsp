<%@ taglib uri="/webwork" prefix="ww" %>
<%@ taglib uri="http://www.extremecomponents.org" prefix="ec" %>
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c' %>
<%@ taglib uri="continuum" prefix="c1" %>
<%@ taglib uri="/plexusSecuritySystem" prefix="pss" %>

<html>
  <ww:i18n name="localization.Continuum">
    <head>
      <title><ww:text name="projectView.page.title"/></title>
    </head>

    <body>
      <div id="h3">

       <ww:action name="projectGroupTab" executeResult="true">
      <ww:param name="tabName" value="'Members'"/>
    </ww:action>
    <div class="axial">
      <!--
      Scan for new Projects?
      -->
    </div>

    <h3>Member Projects</h3>

    <ec:table items="projectGroup.projects"
              var="project"
              showExports="false"
              showPagination="false"
              showStatusBar="false"
              filterable="false">
      <ec:row highlightRow="true">
        <ec:column property="name" title="summary.projectTable.name" width="48%">
          <ww:url id="projectViewUrl" action="projectView">
            <ww:param name="projectId" value="projectId"/>
          </ww:url>
          <ww:a href="%{projectViewUrl}">${pageScope.project.name}</ww:a>
        </ec:column>
        <ec:column property="editAction" title="Edit" width="1%" sortable="false">
          <center>
            <pss:ifAuthorized permission="continuum-modify-group" resource="${projectGroup.name}">
            <c:choose>
              <c:when
                  test="${pageScope.project.state == 1 || pageScope.project.state == 10 || pageScope.project.state == 2 || pageScope.project.state == 3 || pageScope.project.state == 4}">
                <ww:url id="editProjectUrl" action="projectEdit">
                  <ww:param name="projectId" value="${pageScope.project.id}"/>
                  <ww:param name="projectName" value="${project.name}"/>
                </ww:url>
                <ww:a href="%{editProjectUrl}">
                  <img src="<ww:url value='/images/edit.gif'/>" alt="Edit" title="Edit" border="0">
                </ww:a>
              </c:when>
              <c:otherwise>
                <img src="<ww:url value='/images/edit_disabled.gif'/>" alt="Edit" title="Edit" border="0">
              </c:otherwise>
            </c:choose>
            </pss:ifAuthorized>
            <pss:elseAuthorized>
                <img src="<ww:url value='/images/edit_disabled.gif'/>" alt="Edit" title="Edit" border="0">
            </pss:elseAuthorized>
          </center>
        </ec:column>
        <ec:column property="deleteAction" title="Remove" width="1%" sortable="false">
          <center>
            <pss:ifAuthorized permission="continuum-modify-group" resource="${projectGroup.name}">
            <c:choose>
              <c:when
                  test="${pageScope.project.state == 1 || pageScope.project.state == 10 || pageScope.project.state == 2 || pageScope.project.state == 3 || pageScope.project.state == 4}">
                <ww:url id="removeProjectUrl" action="deleteProject">
                  <ww:param name="projectId" value="${pageScope.project.id}"/>
                  <ww:param name="projectName" value="${pageScope.project.name}"/>
                </ww:url>
                <ww:a href="%{removeProjectUrl">
                  <img src="<ww:url value='/images/delete.gif'/>" alt="Delete" title="Delete" border="0">
                </ww:a>
              </c:when>
              <c:otherwise>
                <img src="<ww:url value='/images/delete_disabled.gif'/>" alt="Delete" title="Delete" border="0">
              </c:otherwise>
            </c:choose>
            </pss:ifAuthorized>
            <pss:elseAuthorized>
                <img src="<ww:url value='/images/delete_disabled.gif'/>" alt="Delete" title="Delete" border="0">
            </pss:elseAuthorized>
          </center>
        </ec:column>
      </ec:row>
    </ec:table>
  </div>
  </body>
</ww:i18n>
</html>
