<%@ taglib uri="/webwork" prefix="ww" %>
<%@ taglib uri="continuum" prefix="c1" %>
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
        <ww:if test="${projectId > 0}">
            <ww:url id="actionUrl" action="ircProjectNotifierSave" includeContext="false" includeParams="none" />
        </ww:if>
        <ww:else>
            <ww:url id="actionUrl" action="ircProjectGroupNotifierSave" includeContext="false" includeParams="none"/>
        </ww:else>
        <h3>
            <ww:text name="notifier.section.title">
                <ww:param>IRC</ww:param>
            </ww:text>
        </h3>

        <div class="axial">
          <ww:form action="%{actionUrl}" method="post" validate="true">
            <ww:hidden name="notifierId"/>
            <ww:hidden name="projectId"/>
            <ww:hidden name="projectGroupId"/>
            <ww:hidden name="notifierType"/>            
            <table>
              <tbody>
                <ww:textfield label="%{getText('notifier.irc.host.label')}" name="host" required="true"/>
                <ww:textfield label="%{getText('notifier.irc.port.label')}" name="port"/>
                <ww:textfield label="%{getText('notifier.irc.channel.label')}" name="channel" required="true"/>
                <ww:textfield label="%{getText('notifier.irc.nick.label')}" name="nick"/>
                <ww:textfield label="%{getText('notifier.irc.fullName.label')}" name="fullName"/>
                <ww:password label="%{getText('notifier.irc.password.label')}" name="password"/>
                <ww:checkbox label="%{getText('notifier.event.sendOnSuccess')}" name="sendOnSuccess" value="sendOnSuccess" fieldValue="true"/>
                <ww:checkbox label="%{getText('notifier.event.sendOnFailure')}" name="sendOnFailure" value="sendOnFailure" fieldValue="true"/>
                <ww:checkbox label="%{getText('notifier.event.sendOnError')}" name="sendOnError" value="sendOnError" fieldValue="true"/>
                <ww:checkbox label="%{getText('notifier.event.sendOnWarning')}" name="sendOnWarning" value="sendOnWarning" fieldValue="true"/>
              </tbody>
            </table>
            <div class="functnbar3">
              <c1:submitcancel value="%{getText('save')}" cancel="%{getText('cancel')}"/>
            </div>
          </ww:form>
        </div>
      </div>
    </body>
  </ww:i18n>
</html>
