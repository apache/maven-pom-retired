<%@ taglib uri="/webwork" prefix="ww" %>
<html>
  <ww:i18n name="localization.Continuum">
    <head>
        <title><ww:text name="workingCopy.page.title"/></title>
    </head>
    <body>
      <div id="h3">
        <div>
          <p style="border-top: 1px solid transparent; border-bottom: 1px solid #DFDEDE;">
            <a style="border: 1px solid #DFDEDE; padding-left: 1em; padding-right: 1em; text-decoration: none;"
							href='<ww:url action="projectView">
									<ww:param name="projectId" value="projectId"/>
								</ww:url>'>
							<ww:text name="info"/>
						</a>
            <a style="border: 1px solid #DFDEDE; padding-left: 1em; padding-right: 1em; text-decoration: none;"
							href='<ww:url action="buildResults">
									<ww:param name="projectId" value="projectId"/>
								</ww:url>'>
							<ww:text name="builds"/>
						</a>
            <b style="border: 1px solid #DFDEDE; padding-left: 1em; padding-right: 1em;"><ww:text name="workingCopy"/></b>
          </p>
        </div>

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
