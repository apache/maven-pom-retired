<%@ taglib uri="/webwork" prefix="ww" %>
<html>
  <ww:i18n name="localization.Continuum">
    <head>
        <title><ww:text name="releaseProject.page.title"/></title>
    </head>
    <body>
      <h3>
        <ww:text name="releaseProject.section.title">
          <ww:param><ww:property value="projectName"/></ww:param>
        </ww:text>
      </h3>
      <ww:form action="releaseProject" method="post">
        <p>
          <input name="goal" type="radio" value="prepare" checked/><ww:text name="releaseProject.prepareReleaseOption"/>
          <br/>
          <input name="goal" type="radio" value="perform"/><ww:text name="releaseProject.performReleaseOption"/>
          <br/>
          &nbsp;&nbsp;&nbsp;
          <select name="preparedReleaseId">
            <ww:if test="preparedReleaseName != null">
              <option selected value="<ww:property value="preparedReleaseId"/>">
                <ww:property value="preparedReleaseName"/>
              </option>
            </ww:if>
            <option value=""><ww:text name="releaseProject.provideReleaseParameters"/></option>
          </select>
          <br/>
        </p>
        <input name="projectId" type="hidden" value="<ww:property value="projectId"/>"/>
        <ww:submit value="Submit"/>
      </ww:form>
    </body>
  </ww:i18n>
</html>
