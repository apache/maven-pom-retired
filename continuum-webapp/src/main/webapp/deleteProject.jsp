<%@ taglib uri="webwork" prefix="ww" %>
<html>
  <ww:i18n name="localization.Continuum">
    <head>
        <title><ww:text name="delete.page.title"/></title>
    </head>
    <body>
      <div id="axial" class="h3">
        <h3><ww:text name="delete.page.title"/></h3>

        <div class="warningmessage">
          <p>
            <strong>
                <ww:text name="delete.confirmation.message">
                    <ww:param><ww:property value="projectName"/></ww:param>
                </ww:text>
            </strong>
            <ww:form action="deleteProject.action" method="post">
                <ww:hidden name="projectId"/>
                <ww:submit value="%{getText('delete.submit')}"/>
            </ww:form>
          </p>
        </div>
      </div>
    </body>
  </ww:i18n>
</html>
