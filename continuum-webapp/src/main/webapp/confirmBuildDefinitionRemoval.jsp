<%@ taglib uri="/webwork" prefix="ww" %>
<%@ taglib uri="continuum" prefix="c1" %>
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
        <div class="functnbar3">
          <ww:form action="removeProjectBuildDefinition">
            <ww:hidden name="buildDefinitionId"/>
            <ww:hidden name="projectId"/>
            <ww:hidden name="confirmed" value="true"/>
            <c1:submitcancel value="%{getText('delete')}" cancel="%{getText('cancel')}"/>
          </ww:form>
        </div>
      </div>
    </body>
  </ww:i18n>
</html>
