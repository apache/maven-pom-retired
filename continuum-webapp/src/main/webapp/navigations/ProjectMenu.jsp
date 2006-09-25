<%@ taglib uri="/webwork" prefix="ww" %>
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<%@ taglib uri="continuum" prefix="c1" %>

<div>
  <p style="border-top: 1px solid transparent; border-bottom: 1px solid #DFDEDE;">

    <ww:url id="viewUrl" action="projectView">
    	<ww:param name="projectId" value="projectId"/>
    </ww:url>
    <ww:url id="buildResultsUrl" action="buildResults">
    	<ww:param name="projectId" value="projectId"/>
    </ww:url>
    <ww:url id="workingCopyUrl" action="workingCopy">
    	<ww:param name="projectId" value="projectId"/>
    </ww:url>

    <c:choose>
      <c:when test="${param.tab == 'view'}">
        <b class="tabMenuDisabled"><ww:text name="info"/></b>
      </c:when>
      <c:otherwise>
        <ww:a cssClass="tabMenuEnabled" href="%{viewUrl}"><ww:text name="info"/></ww:a>
      </c:otherwise>
    </c:choose>

    <c:choose>
      <c:when test="${param.tab == 'buildResults'}">
        <b class="tabMenuDisabled"><ww:text name="builds"/></b>
      </c:when>
      <c:otherwise>
        <ww:a cssClass="tabMenuEnabled" href="%{buildResultsUrl}"><ww:text name="builds"/></ww:a>
      </c:otherwise>
    </c:choose>

    <c:choose>
      <c:when test="${param.tab == 'workingCopy'}">
        <b class="tabMenuDisabled"><ww:text name="workingCopy"/></b>
      </c:when>
      <c:otherwise>
        <ww:a cssClass="tabMenuEnabled" href="%{workingCopyUrl}"><ww:text name="workingCopy"/></ww:a>
      </c:otherwise>
    </c:choose>

  </p>
</div>
