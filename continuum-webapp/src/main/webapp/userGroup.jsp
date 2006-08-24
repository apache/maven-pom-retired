<%@ taglib uri="/webwork" prefix="ww" %>
<%@ taglib uri="/tld/extremecomponents" prefix="ec" %>
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c' %>
<%@ taglib uri="continuum" prefix="c1" %>
<html>
<ww:i18n name="localization.Continuum">
  <head>
    <title>User Group Management</title>
  </head>

  <body>
  <div id="axial" class="h3">

    <h3><ww:text name="User Group"/></h3>

    <ww:form action="saveUserGroup" method="post">
      <ww:hidden name="userGroupId"/>

      <div class="axial">
        <table border="1" cellspacing="2" cellpadding="3" width="100%">
          <ww:textfield label="Name" name="name"/>
          <ww:textfield label="Description" name="description"/>
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
