<%@ taglib uri="/webwork" prefix="ww" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<ww:set name="companyLogo" value="companyLogo"/>
<c:choose>
  <c:when test="${!empty(companyLogo)}">
    <ww:set name="companyUrl" value="companyUrl"/>
    <c:choose>
      <c:when test="${!empty(companyUrl)}">
        <a href="${companyUrl}">
          <img src="${companyLogo}" title="${companyName}" border="0" alt="${companyName}"/>
        </a>
      </c:when>
      <c:otherwise>
        <img src="${companyLogo}" title="${companyName}" border="0" alt="${companyName}"/>
      </c:otherwise>
    </c:choose>
  </c:when>
  <c:otherwise>
    <img src="/images/asf_logo_wide.gif" title="Apache Software Foundation" border="0" alt="Apache Software Foundation"/>
  </c:otherwise>
</c:choose>
