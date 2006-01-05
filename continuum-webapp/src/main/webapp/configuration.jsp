<%@ taglib uri="webwork" prefix="ww" %>
<html>
  <ww:i18n name="localization.Continuum">
    <head>
        <title><ww:text name="configuration.page.title"/></title>
    </head>
    <body>
      <div id="axial" class="h3">
        <h3><ww:text name="configuration.section.title"/></h3>

        <div class="axial">
          <table border="1" cellspacing="2" cellpadding="3" width="100%">
            <tr class="b">
              <th><ww:text name="configuration.guest.label"/></th>
              <td>
                <ww:text name="configuration.guest.value">
                    <ww:param>
                        <ww:if test="guestAccountEnabled"><ww:text name="enabled"/></ww:if>
                        <ww:else><ww:text name="disabled"/></ww:else>
                    </ww:param>
                </ww:text>
              </td>
            </tr>
            <tr class="b">
              <th><ww:text name="configuration.workingDirectory.label"/></th>
              <td><ww:property value="workingDirectory"/></td>
            </tr>
            <tr class="b">
              <th><ww:text name="configuration.buildOutputDirectory.label"/></th>
              <td><ww:property value="buildOutputDirectory"/></td>
            </tr>
            <tr class="b">
              <th><ww:text name="configuration.baseUrl.label"/></th>
              <td><ww:property value="baseUrl"/></td>
            </tr>
            <tr class="b">
              <th><ww:text name="configuration.companyName.label"/></th>
              <td><ww:property value="companyName"/></td>
            </tr>
            <tr class="b">
              <th><ww:text name="configuration.companyLogo.label"/></th>
              <td><ww:property value="companyLogo"/></td>
            </tr>
            <tr class="b">
              <th><ww:text name="configuration.companyUrl.label"/></th>
              <td><ww:property value="companyUrl"/></td>
            </tr>
          </table>
          <ww:form action="configuration!edit.action" method="post">
              <ww:submit value="%{getText('edit')}"/>
          </ww:form>
        </div>
      </div>
    </body>
  </ww:i18n>
</html>
