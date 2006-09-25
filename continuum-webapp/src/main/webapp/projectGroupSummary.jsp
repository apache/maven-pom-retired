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
          <jsp:param name="tab" value="summary"/>
        </jsp:include>

        <h3>Project Group Actions</h3>

        <div class="functnbar3">
            <ww:url id="buildProjectGroupUrl" action="buildProjectGroup">
              <ww:param name="projectGroupId" value="projectGroupId"/>
            </ww:url>
            <ww:a href="%{buildProjectGroupUrl}">Build</ww:a>
          &nbsp;
            <ww:url id="removeProjectGroupUrl" action="removeProjectGroup">
              <ww:param name="projectGroupId" value="projectGroupId"/>           
              <ww:param name="confirmed" value="false"/>
            </ww:url>
            <ww:a href="%{removeProjectGroupUrl}">Remove</ww:a>
        </div>

        <h3>Projects</h3>

        <ww:action name="projectSummary" executeResult="true" namespace="component">
          <ww:param name="projectGroupId" value="%{projectGroupId}"/>
        </ww:action>

      </div>
    </body>
  </ww:i18n>
</html>
