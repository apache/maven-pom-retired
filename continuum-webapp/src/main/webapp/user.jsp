<%@ taglib uri="/webwork" prefix="ww" %>
<%@ taglib uri="/tld/extremecomponents" prefix="ec" %>
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c' %>
<html>
<ww:i18n name="localization.Continuum">
  <head>
    <title>User</title>
  </head>

  <body>
  <div id="axial" class="h3">

    <div id="h3">
      <h3><ww:text name="Users"/></h3>

      <ww:form action="saveUser" method="post">

        <div class="axial">
          <table border="1" cellspacing="2" cellpadding="3" width="100%">
            <ww:textfield label="Username" name="username"/>
            <ww:textfield label="Full Name" name="fullName"/>
            <ww:textfield label="Email" name="email"/>
            <ww:textfield label="Password" name="password"/>
            <ww:textfield label="Password(again)" name="passwordTwo"/>
            <ww:select  list="userGroups" label="User Group" name="userGroupId"/>
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
