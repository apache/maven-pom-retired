<%@ taglib uri="webwork" prefix="ww" %>
<html>
  <ww:i18n name="localization.Continuum">
    <head>
        <title><ww:text name="deleteBuildDefinition.page.title"/></title>
    </head>
    <body>
      <div id="axial" class="h3">
        <h3><ww:text name="deleteBuildDefinition.section.title"/></h3>

        <div class="warningmessage">
          <p>
            <strong>
                <ww:text name="deleteBuildDefinition.confirmation.message">
                    <ww:param><ww:property value="buildDefinitionId"/></ww:param>
                </ww:text>
            </strong>
          </p>
        </div>
        <ww:form action="deleteBuildDefinition.action" method="post">
            <ww:hidden name="buildDefinitionId"/>
            <ww:hidden name="projectId"/>
            <div class="functnbar3">
            <input type="submit" value="<ww:text name="delete"/>"/>
            <input type="button" name="Cancel" value="<ww:text name="cancel"/>" onClick="history.back()"/>
            </div>
        </ww:form>
      </div>
    </body>
  </ww:i18n>
</html>
