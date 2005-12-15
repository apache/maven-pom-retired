<%@ taglib uri="webwork" prefix="ww" %>
<html>
  <ww:i18n name="localization.Continuum">
    <head>
        <title><ww:text name="login.page.title"/></title>
    </head>
    <body>
      <div id="axial" class="h3">
        <h3><ww:text name="login.section.title"/></h3>
        <ww:form action="login" method="post">
            <ww:textfield label="%{getText('login.username')}" name="username"/>
            <ww:password label="%{getText('login.password')}" name="password"/>
            <ww:submit value="%{getText('login.submit')}"/>
        </ww:form>
      </div>
    </body>
  </ww:i18n>
</html>
