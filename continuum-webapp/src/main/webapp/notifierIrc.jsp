<%@ taglib uri="webwork" prefix="ww" %>
<html>
  <ww:i18n name="localization.Continuum">
    <head>
        <title>
            <ww:text name="notifier.page.title">
                <ww:param>IRC</ww:param>
            </ww:text>
        </title>
    </head>
    <body>
      <div id="axial" class="h3">
        <h3>
            <ww:text name="notifier.section.title">
                <ww:param>IRC</ww:param>
            </ww:text>
        </h3>

        <div class="axial">
            <ww:form action="ircNotifierEdit.action" method="post">
                <ww:hidden name="notifierId"/>
                <ww:hidden name="projectId"/>
                <ww:hidden name="notifierType"/>
                <ww:textfield label="%{getText('notifier.irc.host.label')}" name="host"/>
                <ww:textfield label="%{getText('notifier.irc.port.label')}" name="port"/>
                <ww:textfield label="%{getText('notifier.irc.channel.label')}" name="channel"/>
                <ww:textfield label="%{getText('notifier.irc.nick.label')}" name="nick"/>
                <ww:textfield label="%{getText('notifier.irc.fullName.label')}" name="fullName"/>
                <ww:password label="%{getText('notifier.irc.password.label')}" name="password"/>
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
