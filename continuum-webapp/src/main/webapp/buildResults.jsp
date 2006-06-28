<%@ taglib uri="/webwork" prefix="ww" %>
<%@ taglib uri="/tld/extremecomponents" prefix="ec" %>
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<html>
  <ww:i18n name="localization.Continuum">
    <head>
        <title><ww:text name="buildResults.page.title"/></title>
    </head>
    <body>
      <div id="h3">
        <div>
          <p style="border-top: 1px solid transparent; border-bottom: 1px solid black;">
            <a style="border: 1px solid black; padding-left: 1em; padding-right: 1em; text-decoration:none;" href="<ww:url value="/projectView.action?projectId="/><ww:property value="projectId"/>&projectName=<ww:property value="projectName"/>"><ww:text name="info"/></a>
            <b style="border: 1px solid black; padding-left: 1em; padding-right: 1em;"><ww:text name="builds"/></b>
            <a style="border: 1px solid black; padding-left: 1em; padding-right: 1em; text-decoration:none;" href="<ww:url value="/workingCopy.action?projectId="/><ww:property value="projectId"/>&projectName=<ww:property value="projectName"/>"><ww:text name="workingCopy"/></a>
          </p>
        </div>

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
            <ec:column property="buildNumber" title="buildResults.buildNumber">
                <c:if test="${!(pageScope.buildResult.state == 2)}">
                    ${pageScope.buildResult.buildNumber}
                </c:if>
            </ec:column>
            <ec:column property="startTime" title="buildResults.startTime" cell="date"/>
            <ec:column property="endTime" title="buildResults.endTime" cell="date"/>
            <ec:column property="state" title="buildResults.state" cell="org.apache.maven.continuum.web.view.buildresults.StateCell"/>
            <ec:column property="actions" title="&nbsp;">
              <a href="${pageContext.request.contextPath}/buildResult.action?projectId=<ww:property value="projectId"/>&projectName=<ww:property value="projectName"/>&buildId=${pageScope.buildResult.id}">Result</a>
            </ec:column>
          </ec:row>
        </ec:table>
      </div>
    </body>
  </ww:i18n>
</html>
