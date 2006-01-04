<%@ taglib uri="webwork" prefix="ww" %>
<html>
  <ww:i18n name="localization.Continuum">
    <head>
        <title><ww:text name="notifier.page.add.title"/></title>
    </head>
    <body>
      <div id="axial" class="h3">
        <h3><ww:text name="notifier.section.add.title"/></h3>

        <div class="axial">
          <ww:form action="addNotifier.action" method="post">
            <ww:hidden name="projectId"/>
            <ww:select label="%{getText('notifier.type.label')}" name="notifierType"
                       list="#{ 'mail' : 'Mail', 'irc' : 'IRC', 'jabber' : 'Jabber', 'msn' : 'MSN'}"/>
            <ww:hidden name="projectId"/>
            <ww:submit value="%{getText('submit')}"/>
          </ww:form>
        </div>
      </div>
    </body>
  </ww:i18n>
</html>
