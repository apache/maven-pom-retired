<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="/tld/extremecomponents" prefix="ec" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<html>
  <ww:i18n name="localization.Continuum">
    <head>
        <title><ww:text name="buildResult.page.title"/></title>
    </head>
    <body>
      <div id="h3">
        <div>
          <p style="border-top: 1px solid transparent; border-bottom: 1px solid black;">
            <a style="border: 1px solid black; padding-left: 1em; padding-right: 1em; text-decoration:none;" href="<ww:url value="/projectView.action?projectId="/><ww:property value="projectId"/>&projectName=<ww:property value="projectName"/>">Info</a>
            <a style="border: 1px solid black; padding-left: 1em; padding-right: 1em; text-decoration:none;" href="<ww:url value="/buildResults.action?projectId="/><ww:property value="projectId"/>&projectName=<ww:property value="projectName"/>">Builds</a>
            <a style="border: 1px solid black; padding-left: 1em; padding-right: 1em; text-decoration:none;" href="<ww:url value="/workingCopy.action?projectId="/><ww:property value="projectId"/>&projectName=<ww:property value="projectName"/>">Working&nbsp;Copy</a>
          </p>
        </div>

        <h3>
            <ww:text name="buildResult.section.title">
                <ww:param><ww:property value="projectName"/></ww:param>
            </ww:text>
        </h3>
      </div>
    </body>
  </ww:i18n>
</html>
