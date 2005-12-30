<%@ taglib uri="webwork" prefix="ww" %>
<html>
  <ww:i18n name="localization.Continuum">
    <head>
        <title>
            <ww:text name="notifier.page.title">
                <ww:param>Jabber</ww:param>
            </ww:text>
        </title>
    </head>
    <body>
      <div id="axial" class="h3">
        <h3>
            <ww:text name="notifier.section.title">
                <ww:param>Jabber</ww:param>
            </ww:text>
        </h3>

        <div class="axial">
            <ww:form action="jabberNotifierEdit.action" method="post">
                <ww:hidden name="notifierId"/>
                <ww:hidden name="projectId"/>
                <ww:hidden name="notifierType"/>
                <ww:textfield label="%{getText('notifier.jabber.host.label')}" name="host"/>
                <ww:textfield label="%{getText('notifier.jabber.port.label')}" name="port"/>
                <ww:textfield label="%{getText('notifier.jabber.login.label')}" name="login"/>
                <ww:password label="%{getText('notifier.jabber.password.label')}" name="password"/>
                <ww:textfield label="%{getText('notifier.jabber.domainName.label')}" name="domainName"/>
                <ww:textfield label="%{getText('notifier.jabber.address.label')}" name="address"/>
                <ww:checkbox label="%{getText('notifier.jabber.isSslConnection.label')}" name="sslConnection" value="sslConnection" fieldValue="true"/>
                <ww:checkbox label="%{getText('notifier.jabber.isGroup.label')}" name="group" value="group" fieldValue="true"/>
                <ww:checkbox label="%{getText('notifier.event.sendOnSuccess')}" name="sendOnSuccess" value="sendOnSuccess" fieldValue="true"/>
                <ww:checkbox label="%{getText('notifier.event.sendOnFailure')}" name="sendOnFailure" value="sendOnFailure" fieldValue="true"/>
                <ww:checkbox label="%{getText('notifier.event.sendOnError')}" name="sendOnError" value="sendOnError" fieldValue="true"/>
                <ww:checkbox label="%{getText('notifier.event.sendOnWarning')}" name="sendOnWarning" value="sendOnWarning" fieldValue="true"/>
                <ww:submit value="%{getText('save')}"/>
            </ww:form>
        </div>
      </div>
    </body>
  </ww:i18n>
</html>
