<%@ taglib uri="/webwork" prefix="ww" %>
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<%@ taglib uri="continuum" prefix="c1" %>

<div>
  <p style="border-top: 1px solid transparent; border-bottom: 1px solid #DFDEDE;">

    <ww:url id="viewUrl" action="projectView" includeParams="none">
        <ww:param name="projectId" value="projectId"/>
        <ww:param name="tab" value="view"/>
    </ww:url>
    <ww:url id="buildResultsUrl" action="buildResults" includeParams="none">
        <ww:param name="projectId" value="projectId"/>
        <ww:param name="tab" value="buildResults"/>
    </ww:url>
    <ww:url id="workingCopyUrl" action="workingCopy" includeParams="none">
        <ww:param name="projectId" value="projectId"/>
        <ww:param name="tab" value="workingCopy"/>
    </ww:url>

    <c:choose>
      <c:when test="${param.tab == 'view'}">
        <b style="border: 1px solid #DFDEDE; padding-left: 1em; padding-right: 1em;"><ww:text name="info"/></b>
      </c:when>
      <c:otherwise>
        <a style="border: 1px solid #DFDEDE; padding-left: 1em; padding-right: 1em; text-decoration: none;" href="${viewUrl}"><ww:text name="info"/></a>
      </c:otherwise>
    </c:choose>

    <c:choose>
      <c:when test="${param.tab == 'buildResults'}">
        <b style="border: 1px solid #DFDEDE; padding-left: 1em; padding-right: 1em;"><ww:text name="builds"/></b>
      </c:when>
      <c:otherwise>
        <a style="border: 1px solid #DFDEDE; padding-left: 1em; padding-right: 1em; text-decoration: none;" href="${buildResultsUrl}"><ww:text name="builds"/></a>
      </c:otherwise>
    </c:choose>

    <c:choose>
      <c:when test="${param.tab == 'workingCopy'}">
        <b style="border: 1px solid #DFDEDE; padding-left: 1em; padding-right: 1em;"><ww:text name="workingCopy"/></b>
      </c:when>
      <c:otherwise>
        <a style="border: 1px solid #DFDEDE; padding-left: 1em; padding-right: 1em; text-decoration: none;" href="${workingCopyUrl}"><ww:text name="workingCopy"/></a>
      </c:otherwise>
    </c:choose>

  </p>
</div>
