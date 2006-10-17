<%@ taglib uri="/webwork" prefix="ww" %>
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<%@ taglib uri="continuum" prefix="c1" %>

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

    <c:choose>
      <c:when test="${param.tab == 'summary'}">
        <b class="tabMenuDisabled">Summary</b>
      </c:when>
      <c:otherwise>
        <ww:a cssClass="tabMenuEnabled" href="%{projectGroupSummaryUrl}">Summary</ww:a>
      </c:otherwise>
    </c:choose>

    <c:choose>
      <c:when test="${param.tab == 'members'}">
        <b class="tabMenuDisabled">Members</b>
      </c:when>
      <c:otherwise>
        <ww:a cssClass="tabMenuEnabled" href="%{projectGroupMembersUrl}">Members</ww:a>
      </c:otherwise>
    </c:choose>

    <c:choose>
      <c:when test="${param.tab == 'buildDefinition'}">
        <b class="tabMenuDisabled">Build Definitions</b>
      </c:when>
      <c:otherwise>
        <ww:a cssClass="tabMenuEnabled" href="%{projectGroupBuildDefinitionUrl}">Build Definitions</ww:a>
      </c:otherwise>
    </c:choose>

<%--
    <c:choose>
      <c:when test="${param.tab == 'notifier'}">
        <b class="tabMenuDisabled">Notifiers</b>
      </c:when>
      <c:otherwise>
        <ww:a cssClass="tabMenuEnabled" href="%{projectGroupNotifierUrl}">Notifiers</ww:a>
      </c:otherwise>
    </c:choose>
--%>

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
