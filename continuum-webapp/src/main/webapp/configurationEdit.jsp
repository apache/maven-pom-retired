<%@ taglib uri="/webwork" prefix="ww" %>
<%@ taglib uri="continuum" prefix="c1" %>
<html>
<ww:i18n name="localization.Continuum">
  <head>
    <title><ww:text name="configuration.page.title"/></title>
  </head>

  <body>
  <div id="axial" class="h3">
    <h3><ww:text name="configuration.section.title"/></h3>
    <ww:form action="configuration.action" method="post">

      <div class="axial">
        <table border="1" cellspacing="2" cellpadding="3" width="100%">
          <ww:textfield label="Username" name="username"/>
          <ww:textfield label="Full Name" name="fullName"/>
          <ww:textfield label="Email" name="email"/>
          <ww:password label="Password" name="password"/>
          <ww:password label="Password(again)" name="passwordTwo"/>
        </table>
      </div>

      <div class="axial">

        <table>
          <tbody>
            <ww:checkbox label="%{getText('configuration.guest.label')}" name="guestAccountEnabled"
                         value="guestAccountEnabled" fieldValue="true"/>
            <ww:textfield label="%{getText('configuration.workingDirectory.label')}" name="workingDirectory"
                          required="true"/>
            <ww:textfield label="%{getText('configuration.buildOutputDirectory.label')}" name="buildOutputDirectory"
                          required="true"/>
            <ww:textfield label="%{getText('configuration.baseUrl.label')}" name="baseUrl" required="true"/>
            <ww:textfield label="%{getText('configuration.companyName.label')}" name="companyName"/>
            <ww:textfield label="%{getText('configuration.companyLogo.label')}" name="companyLogo"/>
            <ww:textfield label="%{getText('configuration.companyUrl.label')}" name="companyUrl"/>
          </tbody>
        </table>
        <div class="functnbar3">
          <c1:submitcancel value="%{getText('save')}" cancel="%{getText('cancel')}"/>
        </div>

      </div>
    </ww:form>
  </div>
  </body>
</ww:i18n>
</html>
