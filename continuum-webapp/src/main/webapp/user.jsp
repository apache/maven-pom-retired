<%@ taglib uri="/webwork" prefix="ww" %>
<%@ taglib uri="http://www.extremecomponents.org" prefix="ec" %>
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c' %>
<%@ taglib uri="continuum" prefix="c1" %>
<html>
<ww:i18n name="localization.Continuum">
  <head>
    <title>User</title>
  </head>

  <body>
  <div id="axial" class="h3">

    <h3><ww:text name="Users"/></h3>

    <ww:form action="saveUser" method="post" validate="true">
      <ww:hidden name="userId"/>

      <h3>Action Error</h3>
      <p>
        <ww:actionerror/>
      </p>

      <div class="axial">
        <table border="1" cellspacing="2" cellpadding="3" width="100%">

          <ww:textfield label="%{getText('user.username.label')}" name="username" required="true">
            <ww:param name="desc"><p><ww:text name="user.username.message"/></p></ww:param>
          </ww:textfield>

          <ww:textfield label="%{getText('user.fullName.label')}" name="fullName" required="true">
            <ww:param name="desc"><p><ww:text name="user.fullName.message"/></p></ww:param>
          </ww:textfield>

          <ww:textfield label="%{getText('user.email.label')}" name="email">
            <ww:param name="desc"><p><ww:text name="user.email.message"/></p></ww:param>
          </ww:textfield>

          <ww:password label="%{getText('user.password.label')}" name="password" required="true"/>

          <ww:password label="%{getText('user.passwordTwo.label')}" name="passwordTwo" required="true">
            <ww:param name="desc"><p><ww:text name="user.passwordTwo.message"/></p></ww:param>
          </ww:password>

          <ww:select list="userGroups" label="%{getText('user.userGroup.label')}" name="userGroupId">
            <ww:param name="desc"><p><ww:text name="user.userGroup.message"/></p></ww:param>
          </ww:select>

        </table>
      </div>

      <div class="functnbar3">
        <c1:submitcancel value="%{getText('save')}" cancel="%{getText('cancel')}"/>
      </div>

    </ww:form>

  </div>
  </body>
</ww:i18n>
</html>
