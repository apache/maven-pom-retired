<%@ taglib uri="/webwork" prefix="ww" %>
<%@ taglib uri="http://www.extremecomponents.org" prefix="ec" %>
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c' %>
<%@ taglib uri="continuum" prefix="c1" %>
<%@ taglib uri="/plexusSecuritySystem" prefix="pss" %>

<ww:i18n name="localization.Continuum">
<ww:if test="${not empty projects}">

  <h3><ww:text name="projectGroup.projects.title"/></h3>

  <ec:table items="projects"
            var="project"
            showExports="false"
            showPagination="false"
            showStatusBar="false"
            sortable="false"
            filterable="false">
    <ec:row highlightRow="true">

      <%-- needed to access project in included pages --%>
      <c:set var="project" value="${pageScope.project}" scope="request"/>

      <ec:column property="state" title="&nbsp;" width="1%" cell="org.apache.maven.continuum.web.view.StateCell"/>
      <ec:column property="name" title="summary.projectTable.name" width="48%">
        <ww:url id="projectUrl" action="projectView" namespace="/">
          <ww:param name="projectId" value="${project.id}"/>
        </ww:url>
        <ww:a href="%{projectUrl}">${pageScope.project.name}</ww:a>
      </ec:column>
      <ec:column property="version" title="summary.projectTable.version" width="13%"/>
      <ec:column property="buildNumber" title="summary.projectTable.build" width="5%"
                 cell="org.apache.maven.continuum.web.view.BuildCell"/>
      <ec:column property="projectGroupName" title="summary.projectTable.group" width="13%"/> 
      <ec:column property="buildNowAction" title="&nbsp;" width="1%">
        <pss:ifAuthorized permission="continuum-build-group" resource="${projectGroupName}">
          <c:choose>
            <c:when test="${!project.inQueue and ( project.state gt 0 ) and ( project.state lt 5 )}">
              <ww:url id="buildProjectUrl" action="buildProject" namespace="/">
                <ww:param name="projectId" value="${project.id}"/>
              </ww:url>
              <ww:a href="%{buildProjectUrl}">
                <img src="<ww:url value='/images/buildnow.gif'/>" alt="Build Now" title="Build Now" border="0">
              </ww:a>
            </c:when>
            <c:otherwise>
              <img src="<ww:url value='/images/buildnow_disabled.gif'/>" alt="Build Now" title="Build Now" border="0">
            </c:otherwise>
          </c:choose>
        </pss:ifAuthorized>
        <pss:elseAuthorized>
          <img src="<ww:url value='/images/buildnow_disabled.gif'/>" alt="Build Now" title="Build Now" border="0">
        </pss:elseAuthorized>
      </ec:column>
      <ec:column property="buildHistoryAction" title="&nbsp;" width="1%">
        <pss:ifAuthorized permission="continuum-modify-group" resource="${projectGroupName}">
        <c:choose>
          <c:when test="${pageScope.project.latestBuildId > 0}">
            <ww:url id="buildResultsUrl" action="buildResults" namespace="/">
              <ww:param name="projectId" value="${project.id}"/>
              <ww:param name="projectName">${project.name}</ww:param>
            </ww:url>
            <ww:a href="%{buildResultsUrl}"><img src="<ww:url value='/images/buildhistory.gif'/>" alt="Build History"
                                                 title="Build History" border="0"></ww:a>
          </c:when>
          <c:otherwise>
            <img src="<ww:url value='/images/buildhistory_disabled.gif'/>" alt="Build History" title="Build History"
                 border="0">
          </c:otherwise>
        </c:choose>
        </pss:ifAuthorized>
        <pss:elseAuthorized>
          <img src="<ww:url value='/images/buildhistory_disabled.gif'/>" alt="Build History" title="Build History"
                 border="0">
        </pss:elseAuthorized>
      </ec:column>
      <ec:column property="workingCopyAction" title="&nbsp;" width="1%">
        <pss:ifAuthorized permission="continuum-modify-group" resource="${projectGroupName}">
        <c:choose>
          <c:when test="${pageScope.project.state == 10 || pageScope.project.state == 2 || pageScope.project.state == 3 || pageScope.project.state == 4 || pageScope.project.state == 6}">
            <ww:url id="workingCopyUrl" action="workingCopy" namespace="/">
              <ww:param name="projectId" value="${project.id}"/>
            </ww:url>
            <ww:a href="%{workingCopyUrl}"><img src="<ww:url value='/images/workingcopy.gif'/>" alt="Working Copy"
                                                title="Working Copy" border="0"></ww:a>
          </c:when>
          <c:otherwise>
            <img src="<ww:url value='/images/workingcopy_disabled.gif'/>" alt="Working Copy" title="Working Copy"
                 border="0">
          </c:otherwise>
        </c:choose>
        </pss:ifAuthorized>
        <pss:elseAuthorized>
          <img src="<ww:url value='/images/workingcopy_disabled.gif'/>" alt="Working Copy" title="Working Copy"
                 border="0">
        </pss:elseAuthorized>
      </ec:column>
      <ec:column property="releaseAction" title="&nbsp;" width="1%" sortable="false">
        <pss:ifAuthorized permission="continuum-modify-group" resource="${projectGroupName}">
        <c:choose>
          <c:when test="${pageScope.project.state == 2}">
            <ww:url id="releaseProjectUrl" action="releasePromptGoal" namespace="/">
              <ww:param name="projectId" value="${project.id}"/>
            </ww:url>
            <ww:a href="%{releaseProjectUrl}">
              <img src="<ww:url value='/images/releaseproject.gif'/>" alt="Release Project" title="Release Project"
                border="0"/>
            </ww:a>
          </c:when>
          <c:otherwise>
            <img src="<ww:url value='/images/releaseproject_disabled.gif'/>" alt="Release Project"
              title="Release Project" border="0"/>
          </c:otherwise>
        </c:choose>
        </pss:ifAuthorized>
        <pss:elseAuthorized>
          <img src="<ww:url value='/images/releaseproject_disabled.gif'/>" alt="Delete" title="Delete" border="0">
        </pss:elseAuthorized>
      </ec:column>
      <ec:column property="deleteAction" title="&nbsp;" width="1%" sortable="false">
        <pss:ifAuthorized permission="continuum-modify-group" resource="${projectGroupName}">
        <c:choose>
          <c:when
              test="${pageScope.project.state == 1 || pageScope.project.state == 10 || pageScope.project.state == 2 || pageScope.project.state == 3 || pageScope.project.state == 4}">
            <ww:url id="deleteProjectUrl" value="deleteProject!default.action" namespace="/">
              <ww:param name="projectId" value="${project.id}"/>
              <ww:param name="projectName" value="${project.name}"/>
            </ww:url>
            <ww:a href="%{deleteProjectUrl}">
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
      </ec:column>
    </ec:row>
  </ec:table>
</ww:if>
</ww:i18n>
