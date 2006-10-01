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

        <div class="axial">
          <table border="1" cellspacing="2" cellpadding="3" width="100%">
            <c1:data label="%{getText('configuration.guest.label')}">
                <ww:param name="after">
                    <ww:text name="configuration.guest.value">
                        <ww:param>
                            <ww:if test="%{guestAccountEnabled == true}"><ww:text name="enabled"/></ww:if>
                            <ww:else><ww:text name="disabled"/></ww:else>
                        </ww:param>
                    </ww:text>
                </ww:param>
            </c1:data>
            <c1:data label="%{getText('configuration.workingDirectory.label')}" name="workingDirectory"/>
            <c1:data label="%{getText('configuration.buildOutputDirectory.label')}" name="buildOutputDirectory"/>
            <c1:data label="%{getText('configuration.deploymentRepositoryDirectory.label')}" name="deploymentRepositoryDirectory"/>
            <c1:data label="%{getText('configuration.baseUrl.label')}" name="baseUrl"/>
            <c1:data label="%{getText('configuration.companyName.label')}" name="companyName"/>
            <c1:data label="%{getText('configuration.companyLogo.label')}" name="companyLogo"/>
            <c1:data label="%{getText('configuration.companyUrl.label')}" name="companyUrl"/>
          </table>
          <div class="functnbar3">
            <ww:form action="editConfiguration.action" method="post">
              <ww:submit value="%{getText('edit')}"/>
            </ww:form>
          </div>
        </div>
      </div>
    </body>
  </ww:i18n>
</html>
