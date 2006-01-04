<%@ taglib uri="webwork" prefix="ww" %>
<html>
  <ww:i18n name="localization.Continuum">
    <head>
        <title><ww:text name="login.page.title"/></title>
    </head>
    <body>
      <div id="axial" class="h3">
        <h3><ww:text name="login.section.title"/></h3>

        <%-- TODO check parameter login_error, when = 1 means that there was an error --%>

        <ww:form action="j_acegi_security_check" method="post">
            <ww:textfield label="%{getText('login.username')}" name="j_username"/>
            <ww:password label="%{getText('login.password')}" name="j_password"/>
            <ww:submit value="%{getText('login.submit')}"/>
        </ww:form>
      </div>
    </body>
  </ww:i18n>
</html>
