<%@ taglib uri="/webwork" prefix="ww" %>
<html>
  <ww:i18n name="localization.Continuum">
    <head>
        <title><ww:text name="releaseProject.page.title"/></title>
        <meta http-equiv="refresh" content="10;url=<ww:url includeParams="all" />"/>
    </head>
    <body>
      <h2><ww:text name="releaseInProgress.section.title"/></h2>
      <h3><ww:property value="name"/></h3>
      <ww:form action="releaseInProgress" method="get">
        <ww:hidden name="projectId"/>
        <ww:hidden name="releaseId"/>
          <p>
            The release goal is currently initializing...
          </p>
          <p>
            Please wait while the server prepares your project for release.
          </p>
        <ww:submit value="Refresh"/>
      </ww:form>
    </body>
  </ww:i18n>
</html>
