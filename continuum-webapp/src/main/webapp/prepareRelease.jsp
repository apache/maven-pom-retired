<%@ taglib uri="/webwork" prefix="ww" %>
<html>
  <ww:i18n name="localization.Continuum">
    <head>
        <title><ww:text name="releaseProject.page.title"/></title>
    </head>
    <body>
      <h2><ww:text name="prepareRelease.section.title"/></h2>
      <ww:form action="prepareRelease!doPrepare.action" method="post">
        <h3><ww:text name="prepareRelease.releaseParameters"/></h3>
        <input type="hidden" name="projectId" value="<ww:property value="projectId"/>"/>
        <div class="axial">
          <table border="1" cellspacing="2" cellpadding="3" width="100%">
            <ww:textfield label="SCM Username" name="scmUsername"/>
            <ww:textfield label="SCM Password" name="scmPassword"/>
            <ww:textfield label="SCM Tag" name="scmTag" required="true"/>
            <ww:textfield label="SCM Tag Base" name="scmTagBase" required="true"/>
            <ww:textfield label="Preparation Goals" name="prepareGoals" required="true"/>
          </table>
        </div>

        <ww:iterator value="projects">
          <h3><ww:property value="name"/></h3>
          <input type="hidden" name="projectKeys" value="<ww:property value="key"/>">
          <div class="axial">
            <table border="1" cellspacing="2" cellpadding="3" width="100%">
              <tr>
                <th><ww:text name="prepareRelease.releaseVersion"/></th>
                <td>
                  <input type=text name="relVersions"
                         value="<ww:property value="release"/>" size="100">
                </td>
              </tr>
              <tr>
                <th><ww:text name="prepareRelease.nextDevelopmentVersion"/></th>
                <td>
                  <input type=text name="devVersions"
                         value="<ww:property value="dev"/>" size="100">
                </td>
              </tr>
             </table>
           </div>
        </ww:iterator>

        <ww:submit/>
      </ww:form>
    </body>
  </ww:i18n>
</html>
