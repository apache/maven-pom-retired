<%@ taglib uri="/webwork" prefix="ww" %>
<%@ taglib uri="/tld/extremecomponents" prefix="ec" %>
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c' %>
<%@ taglib uri="continuum" prefix="c1" %>
<html>
<ww:i18n name="localization.Continuum">
  <head>
    <title><ww:text name="projectView.page.title"/></title>
  </head>

  <body>
  <div id="h3">
    <div>
      <p style="border-top: 1px solid transparent; border-bottom: 1px solid #DFDEDE;">
        <ww:url id="projectGroupSummaryUrl" action="projectGroupSummary">
          <ww:param name="projectGroupId" value="projectGroupId"/>
        </ww:url>
        <ww:url id="projectGroupMembersUrl" action="projectGroupMembers">
          <ww:param name="projectGroupId" value="projectGroupId"/>
        </ww:url>
        <ww:url id="projectGroupBuildDefinitionUrl" action="projectGroupBuildDefinition">
          <ww:param name="projectGroupId" value="projectGroupId"/>
        </ww:url>
        <ww:url id="projectGroupNotifierUrl" action="projectGroupNotifier">
          <ww:param name="projectGroupId" value="projectGroupId"/>
        </ww:url>

        <ww:a cssStyle="border: 1px solid #DFDEDE; padding-left: 1em; padding-right: 1em; text-decoration: none;"
              href="%{projectGroupSummaryUrl}">Summary</ww:a>
        <b style="border: 1px solid #DFDEDE; padding-left: 1em; padding-right: 1em;">Members</b>
        <ww:a cssStyle="border: 1px solid #DFDEDE; padding-left: 1em; padding-right: 1em; text-decoration: none;"
              href="%{projectGroupBuildDefinitionUrl}">Build Definitions</ww:a>
        <ww:a cssStyle="border: 1px solid #DFDEDE; padding-left: 1em; padding-right: 1em; text-decoration: none;"
              href="%{projectGroupNotifierUrl}">Notifiers</ww:a>
      </p>
    </div>

    <h3>Project Group Information</h3>

    <div class="axial">
      <table border="1" cellspacing="2" cellpadding="3" width="100%">
        <c1:data label="%{getText('projectView.project.name')}" name="projectGroup.name"/>
        <c1:data label="Group Id" name="projectGroup.groupId"/>
        <c1:data label="Description" name="projectGroup.description"/>
      </table>
    </div>

    <h3>Membership Actions</h3>

    <div class="axial">
      Scan for new Projects? 
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
          </center>
        </ec:column>
        <ec:column property="deleteAction" title="Remove" width="1%" sortable="false">
          <center>
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
          </center>
        </ec:column>
      </ec:row>
    </ec:table>
  </div>
  </body>
</ww:i18n>
</html>
