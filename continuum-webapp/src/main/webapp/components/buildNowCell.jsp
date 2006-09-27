<%@ taglib uri="/webwork" prefix="ww" %>
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<%@ taglib uri="continuum" prefix="c1" %>

<ww:i18n name="localization.Continuum">

  <c1:ifAuthorized permission="buildProject">
    <c:choose>
      <c:when test="${!project.inQueue and ( project.state gt 0 ) and ( project.state lt 5 )}">
        <ww:url id="buildProjectUrl" action="buildProject" namespace="/">
          <ww:param name="projectId" value="${project.id}"/>
        </ww:url>
        <ww:a href="%{buildProjectUrl}">
          <img src="<c:url value='/images/buildnow.gif'/>" alt="Build Now" title="Build Now" border="0">
        </ww:a>
      </c:when>
      <c:otherwise>
        <img src="<c:url value='/images/buildnow_disabled.gif'/>" alt="Build Now" title="Build Now" border="0">
      </c:otherwise>
    </c:choose>
  </c1:ifAuthorized>

</ww:i18n>
