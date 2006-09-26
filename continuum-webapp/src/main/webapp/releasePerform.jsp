<%@ taglib uri="/webwork" prefix="ww" %>
<html>
  <ww:i18n name="localization.Continuum">
    <head>
        <title><ww:text name="release.page.title"/></title>
    </head>
    <body>
      <h2><ww:text name="releasePerform.section.title"/></h2>
      <ww:form action="releasePerform" method="post" validate="true">
        <h3><ww:text name="releasePerform.parameters"/></h3>
        <ww:hidden name="projectId"/>
        <ww:hidden name="releaseId"/>
        <div class="axial">
          <table border="1" cellspacing="2" cellpadding="3" width="100%">
            <ww:textfield label="Maven Arguments" name="goals" value="clean deploy"/>
            <ww:checkbox label="Use Release Profile" name="useReleaseProfile" value="true"/>
          </table>
        </div>
        <ww:submit/>
      </ww:form>
    </body>
  </ww:i18n>
</html>
