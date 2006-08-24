<%@ taglib uri="/webwork" prefix="ww" %>
<%@ taglib uri="continuum" prefix="c1" %>
<html>
  <ww:i18n name="localization.Continuum">
    <head>
        <title><ww:text name="login.page.title"/></title>
    </head>
    <body>
      <div id="axial" class="h3">
        <h3><ww:text name="login.section.title"/></h3>
        <div class="axial">
        <ww:form action="login" method="post">
          <ww:hidden name="login" value="true"/>
          <ww:actionerror/>
          <table>
            <tbody>
              <ww:textfield label="%{getText('login.username')}" name="username" required="true"/>
              <ww:password label="%{getText('login.password')}" name="password" required="true"/>
              <ww:checkbox label="%{getText('login.rememberMe')}" name="rememberMe" value="rememberMe" fieldValue="true"/>
            </tbody>
          </table>
          <div class="functnbar3">
            <c1:submitcancel value="%{getText('login.submit')}" cancel="%{getText('cancel')}"/>
          </div>
        </ww:form>
        </div>
      </div>
    </body>
  </ww:i18n>
</html>
