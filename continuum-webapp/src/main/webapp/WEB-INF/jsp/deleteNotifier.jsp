<%@ taglib uri="/webwork" prefix="ww" %>
<%@ taglib uri="continuum" prefix="c1" %>
<html>
  <ww:i18n name="localization.Continuum">
    <head>
        <title><ww:text name="deleteNotifier.page.title"/></title>
    </head>
    <body>
      <div id="axial" class="h3">
        <h3><ww:text name="deleteNotifier.section.title"/></h3>

        <div class="warningmessage">
          <p>
            <strong>
                <ww:text name="deleteNotifier.confirmation.message">
                    <ww:param><ww:property value="notifierType"/></ww:param>
                    <ww:param><ww:property value="notifierId"/></ww:param>
                </ww:text>
            </strong>
          </p>
        </div>
        <div class="functnbar3">
          <ww:form action="deleteProjectNotifier.action" method="post">
            <ww:hidden name="notifierId"/>
            <ww:hidden name="projectId"/>
            <ww:hidden name="projectGroupId" />
            <c1:submitcancel value="%{getText('delete')}" cancel="%{getText('cancel')}"/>
          </ww:form>
        </div>
      </div>
    </body>
  </ww:i18n>
</html>
