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
          <ww:textfield label="Username" name="username" required="true"/>
          <ww:textfield label="Full Name" name="fullName" required="true"/>
          <ww:textfield label="Email" name="email"/>
          <ww:password label="Password" name="password" required="true"/>
          <ww:password label="Password(again)" name="passwordTwo" required="true"/>
          <ww:select list="userGroups" label="User Group" name="userGroupId"/>
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
