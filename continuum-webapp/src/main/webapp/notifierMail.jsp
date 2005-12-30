<%@ taglib uri="webwork" prefix="ww" %>
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
            <ww:form action="mailNotifierEdit.action" method="post">
                <ww:hidden name="notifierId"/>
                <ww:hidden name="projectId"/>
                <ww:textfield label="%{getText('notifier.mail.recipient.label')}" name="address"/>
                <ww:checkbox label="%{getText('notifier.event.sendOnSuccess')}" name="sendOnSuccess" fieldValue="true"/>
                <ww:checkbox label="%{getText('notifier.event.sendOnFailure')}" name="sendOnFailure" fieldValue="true"/>
                <ww:checkbox label="%{getText('notifier.event.sendOnError')}" name="sendOnError" fieldValue="true"/>
                <ww:checkbox label="%{getText('notifier.event.sendOnWarning')}" name="sendOnWarning" fieldValue="true"/>
                <ww:submit value="%{getText('save')}"/>
            </ww:form>
        </div>
      </div>
    </body>
  </ww:i18n>
</html>
