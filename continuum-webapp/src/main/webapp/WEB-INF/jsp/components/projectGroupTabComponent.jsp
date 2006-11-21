<%@ taglib uri="/webwork" prefix="ww" %>
<%@ taglib uri="http://www.extremecomponents.org" prefix="ec" %>
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<%@ taglib uri="continuum" prefix="c1" %>

  <div id="h3">
    <div>
      <p style="border-top: 1px solid transparent; border-bottom: 1px solid #DFDEDE;">
       <ww:url id="projectGroupSummaryUrl" action="projectGroupSummary" includeParams="none">
          <ww:param name="projectGroupId" value="projectGroupId"/>
        </ww:url>
        <ww:url id="projectGroupMembersUrl" action="projectGroupMembers" includeParams="none">
          <ww:param name="projectGroupId" value="projectGroupId"/>
        </ww:url>
        <ww:url id="projectGroupBuildDefinitionUrl" action="projectGroupBuildDefinition" includeParams="none">
          <ww:param name="projectGroupId" value="projectGroupId"/>
        </ww:url>
        <ww:url id="projectGroupNotifierUrl" action="projectGroupNotifier" includeParams="none">
          <ww:param name="projectGroupId" value="projectGroupId"/>
        </ww:url>

        <ww:set name="tabName" value="tabName"/>
        <c:choose>
            <c:when test="${tabName != 'Summary'}">
                <a style="border: 1px solid #DFDEDE; padding-left: 1em; padding-right: 1em; text-decoration: none;" href="${projectGroupSummaryUrl}">
            </c:when>
            <c:otherwise>
                <b style="border: 1px solid #DFDEDE; padding-left: 1em; padding-right: 1em;">            
            </c:otherwise>
        </c:choose>
        Project Group Summary
        <c:choose>
            <c:when test="${tabName != 'Summary'}">
                </a>
            </c:when>
            <c:otherwise>
                </b>
            </c:otherwise>
        </c:choose>
        <c:choose>
            <c:when test="${tabName != 'Members'}">
                <a style="border: 1px solid #DFDEDE; padding-left: 1em; padding-right: 1em; text-decoration: none;" href="${projectGroupMembersUrl}">
            </c:when>
            <c:otherwise>
                <b style="border: 1px solid #DFDEDE; padding-left: 1em; padding-right: 1em;">
            </c:otherwise>
        </c:choose>
        Members
        <c:choose>
            <c:when test="${tabName != 'Members'}">
                </a>
            </c:when>
            <c:otherwise>
                </b>
            </c:otherwise>
        </c:choose>
        <c:choose>
            <c:when test="${tabName != 'BuildDefinitions'}">
                <a style="border: 1px solid #DFDEDE; padding-left: 1em; padding-right: 1em; text-decoration: none;" href="${projectGroupBuildDefinitionUrl}">
            </c:when>
            <c:otherwise>
                <b style="border: 1px solid #DFDEDE; padding-left: 1em; padding-right: 1em;">
            </c:otherwise>
        </c:choose>
        Build Definitions
        <c:choose>
            <c:when test="${tabName != 'BuildDefinitions'}">
                </a>
            </c:when>
            <c:otherwise>
                </b>
            </c:otherwise>
        </c:choose>
        <c:choose>
            <c:when test="${tabName != 'Notifier'}">
                <a style="border: 1px solid #DFDEDE; padding-left: 1em; padding-right: 1em; text-decoration: none;" href="${projectGroupNotifierUrl}">
            </c:when>
            <c:otherwise>
                <b style="border: 1px solid #DFDEDE; padding-left: 1em; padding-right: 1em;">
            </c:otherwise>
        </c:choose>
        Notifiers
        <c:choose>
            <c:when test="${tabName != 'Notifier'}">
                </a>
            </c:when>
            <c:otherwise>
                </b>
            </c:otherwise>
        </c:choose>
      </p>
    </div>
  </div>
