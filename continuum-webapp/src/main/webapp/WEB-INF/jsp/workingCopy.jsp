<%@ taglib uri="/webwork" prefix="ww" %>
<html>
  <ww:i18n name="localization.Continuum">
    <head>
        <title><ww:text name="workingCopy.page.title"/></title>
    </head>
    <body>
      <div id="h3">

        <jsp:include page="/WEB-INF/jsp/navigations/ProjectMenu.jsp">
          <jsp:param name="tab" value="workingCopy"/>
        </jsp:include>

        <h3>
            <ww:text name="workingCopy.section.title">
                <ww:param><ww:property value="projectName"/></ww:param>
            </ww:text>
        </h3>

        <ww:property value="output" escape="false"/>

        <%
            if ( request.getParameter( "file" ) != null )
            {
        %>
        <br />
        <form>
          <textarea rows="50" cols="100"><ww:property value="fileContent"/></textarea>
        </form>
        <%
            }
        %>

      </div>
    </body>
  </ww:i18n>
</html>
