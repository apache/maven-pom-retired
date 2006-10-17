<%@ taglib uri="/webwork" prefix="ww" %>
<%@ taglib uri="http://www.extremecomponents.org" prefix="ec" %>
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<html>
  <ww:i18n name="localization.Continuum">
    <head>
        <title><ww:text name="buildResults.page.title"/></title>
    </head>
    <body>
      <div id="h3">

        <jsp:include page="/WEB-INF/jsp/navigations/ProjectMenu.jsp">
          <jsp:param name="tab" value="buildResults"/>
        </jsp:include>

        <h3>
            <ww:text name="buildResults.section.title">
                <ww:param><ww:property value="projectName"/></ww:param>
            </ww:text>
        </h3>
        <ww:set name="buildResults" value="buildResults" scope="request"/>
        <ec:table items="buildResults"
                  var="buildResult"
                  showExports="false"
                  showPagination="false"
                  showStatusBar="false"
                  filterable="false"
                  sortable="false">
          <ec:row highlightRow="true">
            <ec:column property="buildNumberIfNotZero" title="buildResults.buildNumber">
                <c:if test="${pageScope.buildResult.state == 2}">
                    <c:out value="${pageScope.buildResult.buildNumber}"/>
                </c:if>
            </ec:column>
            <ec:column property="startTime" title="buildResults.startTime" cell="date"/>
            <ec:column property="endTime" title="buildResults.endTime" cell="date"/>
            <ec:column property="state" title="buildResults.state" cell="org.apache.maven.continuum.web.view.buildresults.StateCell"/>
            <ec:column property="actions" title="&nbsp;">
              <ww:url id="buildResultUrl" action="buildResult">
                <ww:param name="projectId" value="${projectId}"/>
                <ww:param name="projectName" value="%{projectName}"/>
                <ww:param name="buildId" value="${buildResult.id}"/>
              </ww:url>
              <ww:a href="%{buildResultUrl}">Result</ww:a>
            </ec:column>
          </ec:row>
        </ec:table>
      </div>
    </body>
  </ww:i18n>
</html>
