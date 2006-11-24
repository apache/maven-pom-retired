<%@ taglib uri="/webwork" prefix="ww" %>
<%@ taglib uri="http://www.extremecomponents.org" prefix="ec" %>
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<%@ taglib uri="continuum" prefix="c1" %>
<%@ taglib uri="/plexusSecuritySystem" prefix="pss" %>

<ww:i18n name="localization.Continuum">

  <h3>Project Group Build Definitions</h3>
  <ec:table items="groupBuildDefinitionSummaries"
            var="buildDefinitionSummary"
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
      <ec:column property="scheduleName" title="schedule"/>
      <ec:column property="from" title="projectView.buildDefinition.from"/>
      <ec:column property="isDefault" title="projectView.buildDefinition.default"/>
      <ec:column property="editActions" title="&nbsp;">
        <center>
        <pss:ifAuthorized permission="continuum-modify-group" resource="${projectGroupName}">
          <ww:url id="editUrl" action="buildDefinition" method="input" namespace="/">
            <ww:param name="projectGroupId">${pageScope.buildDefinitionSummary.projectGroupId}</ww:param>
            <ww:param name="buildDefinitionId">${pageScope.buildDefinitionSummary.id}</ww:param>
          </ww:url>
          <ww:a href="%{editUrl}">
              <img src="<ww:url value='/images/edit.gif'/>" alt="Edit" title="Edit" border="0">
          </ww:a>
        </pss:ifAuthorized>
        <pss:elseAuthorized>
          <img src="<ww:url value='/images/edit_disabled.gif'/>" alt="Edit" title="Edit" border="0">
        </pss:elseAuthorized>
        </center>
      </ec:column>    
      <ec:column property="deleteActions" title="&nbsp;">
        <center>
        <pss:ifAuthorized permission="continuum-modify-group" resource="${projectGroupName}">
          <ww:url id="removeUrl" action="removeGroupBuildDefinition" namespace="/">
            <ww:param name="projectGroupId">${pageScope.buildDefinitionSummary.projectGroupId}</ww:param>
            <ww:param name="buildDefinitionId">${pageScope.buildDefinitionSummary.id}</ww:param>
            <ww:param name="confirmed" value="false"/>
          </ww:url>
        <ww:a href="%{removeUrl}">
            <img src="<ww:url value='/images/delete.gif'/>" alt="Delete" title="Delete" border="0">
        </ww:a>
        </pss:ifAuthorized>
        <pss:elseAuthorized>
          <img src="<ww:url value='/images/delete_disabled.gif'/>" alt="Delete" title="Delete" border="0">
        </pss:elseAuthorized>
        </center>
      </ec:column>
    </ec:row>
  </ec:table>
  <div class="functnbar3">
    <pss:ifAuthorized permission="continuum-modify-group" resource="${projectGroupName}">
    <ww:form action="buildDefinition" method="post">
      <input type="hidden" name="projectGroupId" value="<ww:property value="projectGroupId"/>"/>
      <ww:submit value="%{getText('add')}"/>
    </ww:form>
    </pss:ifAuthorized>
  </div>

  <ww:if test="${not empty projectBuildDefinitionSummaries}">
  <h3>Project Build Definitions</h3>

  <ec:table items="projectBuildDefinitionSummaries"
            var="buildDefinitionSummary"
            showExports="false"
            showPagination="false"
            showStatusBar="false"
            filterable="false"
            sortable="false">
    <ec:row>
      <ec:column property="projectName" title="Project"/>
      <ec:column property="goals" title="projectView.buildDefinition.goals"/>
      <ec:column property="arguments" title="projectView.buildDefinition.arguments"/>
      <ec:column property="buildFile" title="projectView.buildDefinition.buildFile"/>
      <ec:column property="profile" title="projectView.buildDefinition.profile"/>
      <ec:column property="scheduleName" title="schedule"/>
      <ec:column property="from" title="projectView.buildDefinition.from"/>
      <ec:column property="isDefault" title="projectView.buildDefinition.default"/>
      <ec:column property="buildNowAction" title="&nbsp;" width="1%">
        <ww:url id="buildProjectUrl" action="buildProject" namespace="/">
          <ww:param name="projectId" value="${project.id}"/>
        </ww:url>
        <ww:a href="%{buildProjectUrl}">
          <img src="<c:url value='/images/buildnow.gif'/>" alt="Build Now" title="Build Now" border="0">
        </ww:a>
      </ec:column>
      <ec:column property="editAction" title="&nbsp;">
        <pss:ifAuthorized permission="continuum-modify-group" resource="${projectGroupName}">
          <ww:url id="editUrl" action="buildDefinition" method="input" namespace="/">
            <ww:param name="projectId">${pageScope.buildDefinitionSummary.projectId}</ww:param>
            <ww:param name="buildDefinitionId">${pageScope.buildDefinitionSummary.id}</ww:param>
          </ww:url>
          <ww:a href="%{editUrl}">
              <img src="<ww:url value='/images/edit.gif'/>" alt="Edit" title="Edit" border="0">          
          </ww:a>
        </pss:ifAuthorized>
        <pss:elseAuthorized>
          <img src="<ww:url value='/images/edit_disabled.gif'/>" alt="Edit" title="Edit" border="0">
        </pss:elseAuthorized>
      </ec:column>
      <ec:column property="removeAction" title="&nbsp;">
        <pss:ifAuthorized permission="continuum-modify-group" resource="${projectGroupName}">
          <ww:url id="removeUrl" action="removeProjectBuildDefinition" namespace="/">
            <ww:param name="projectId">${pageScope.buildDefinitionSummary.projectId}</ww:param>
            <ww:param name="buildDefinitionId">${pageScope.buildDefinitionSummary.id}</ww:param>
            <ww:param name="confirmed" value="false"/>
          </ww:url>
          <ww:a href="%{removeUrl}">
              <img src="<ww:url value='/images/delete.gif'/>" alt="Edit" title="Edit" border="0">
          </ww:a>
        </pss:ifAuthorized>
        <pss:elseAuthorized>
           <img src="<ww:url value='/images/delete_disabled.gif'/>" alt="Edit" title="Edit" border="0"> 
        </pss:elseAuthorized>
      </ec:column>
    </ec:row>
  </ec:table>

  </ww:if>

</ww:i18n>
