<%@ taglib uri="/webwork" prefix="ww" %>
<%@ taglib uri="http://www.extremecomponents.org" prefix="ec" %>
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<%@ taglib uri="continuum" prefix="c1" %>
<html>
  <ww:i18n name="localization.Continuum">
    <head>
      <title><ww:text name="projectView.page.title"/></title>
    </head>

    <body>
      <div id="h3">

        <jsp:include page="/navigations/ProjectGroupMenu.jsp">
          <jsp:param name="tab" value="buildDefinition"/>
        </jsp:include>

        <ww:action name="groupBuildDefinitionSummary" executeResult="true" namespace="component">
          <ww:param name="projectGroupId" value="%{projectGroupId}"/>
        </ww:action>
      </div>
    </body>
  </ww:i18n>
</html>
