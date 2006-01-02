<%@ taglib uri="webwork" prefix="ww" %>
<html>
  <ww:i18n name="localization.Continuum">
    <head>
        <title><ww:text name="workingCopy.page.title"/></title>
    </head>
    <body>
      <div id="h3">
        <div>
          <p style="border-top: 1px solid transparent; border-bottom: 1px solid black;">
            <a style="border: 1px solid black; padding-left: 1em; padding-right: 1em; text-decoration:none;" href="<ww:url value="/projectView.action?projectId="/><ww:property value="projectId"/>&projectName=<ww:property value="projectName"/>">Info</a>
            <a style="border: 1px solid black; padding-left: 1em; padding-right: 1em; text-decoration:none;" href="<ww:url value="/buildResults.action?projectId="/><ww:property value="projectId"/>&projectName=<ww:property value="projectName"/>">Builds</a>
            <b style="border: 1px solid black; padding-left: 1em; padding-right: 1em;">Working&nbsp;Copy</b>
          </p>
        </div>

        <h3>
            <ww:text name="workingCopy.section.title">
                <ww:param><ww:property value="projectName"/></ww:param>
            </ww:text>
        </h3>

        <ww:property value="output"/>
      </div>
    </body>
  </ww:i18n>
</html>
