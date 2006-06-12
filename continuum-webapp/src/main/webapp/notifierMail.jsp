<%@ taglib uri="/webwork" prefix="ww" %>
<%@ taglib uri="continuum" prefix="c1" %>
<html>
  <ww:i18n name="localization.Continuum">
    <head>
        <title>
            <ww:text name="notifier.page.title">
                <ww:param>Mail</ww:param>
            </ww:text>
        </title>
    </head>
    <body>
      <div id="axial" class="h3">
        <h3>
            <ww:text name="notifier.section.title">
                <ww:param>Mail</ww:param>
            </ww:text>
        </h3>

        <div class="axial">
            <ww:form action="mailNotifierSave.action" method="post">
                <ww:hidden name="notifierId"/>
                <ww:hidden name="projectId"/>
                <ww:hidden name="notifierType"/>
                <ww:textfield label="%{getText('notifier.mail.recipient.label')}" name="address" required="true"/>
                <ww:checkbox label="%{getText('notifier.event.sendOnSuccess')}" name="sendOnSuccess" value="sendOnSuccess" fieldValue="true"/>
                <ww:checkbox label="%{getText('notifier.event.sendOnFailure')}" name="sendOnFailure" value="sendOnFailure" fieldValue="true"/>
                <ww:checkbox label="%{getText('notifier.event.sendOnError')}" name="sendOnError" value="sendOnError" fieldValue="true"/>
                <ww:checkbox label="%{getText('notifier.event.sendOnWarning')}" name="sendOnWarning" value="sendOnWarning" fieldValue="true"/>
                <c1:submitcancel value="%{getText('save')}" cancel="%{getText('cancel')}"/>
            </ww:form>
        </div>
      </div>
    </body>
  </ww:i18n>
</html>
