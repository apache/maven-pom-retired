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
            <a style="border: 1px solid black; padding-left: 1em; padding-right: 1em; text-decoration:none;" href="<ww:url value="/projectView.action?projectId="/><ww:property value="projectId"/>&projectName=<ww:property value="projectName"/>"><ww:text name="info"/></a>
            <a style="border: 1px solid black; padding-left: 1em; padding-right: 1em; text-decoration:none;" href="<ww:url value="/buildResults.action?projectId="/><ww:property value="projectId"/>&projectName=<ww:property value="projectName"/>"><ww:text name="builds"/></a>
            <a style="border: 1px solid black; padding-left: 1em; padding-right: 1em; text-decoration:none;" href="<ww:url value="/workingCopy.action?projectId="/><ww:property value="projectId"/>&projectName=<ww:property value="projectName"/>"><ww:text name="workingCopy"/></a>
          </p>
        </div>

        <h3>
            <ww:text name="buildResult.section.title">
                <ww:param><ww:property value="projectName"/></ww:param>
            </ww:text>
        </h3>

        <div class="axial">
          <table border="1" cellspacing="2" cellpadding="3" width="100%">
            <tr class="b">
              <th><ww:text name="buildResult.startTime"/></th>
              <td>TODO<ww:property value="buildResult.startTime"/></td>
            </tr>
            <tr class="b">
              <th><ww:text name="buildResult.endTime"/></th>
              <td>TODO<ww:property value="buildResult.endTime"/></td>
            </tr>
            <tr class="b">
              <th><ww:text name="buildResult.buildTrigger"/></th>
              <td>TODO<ww:property value="buildResult.trigger"/></td>
            </tr>
            <tr class="b">
              <th><ww:text name="buildResult.state"/></th>
              <td>TODO<ww:property value="buildResult.state"/></td>
            </tr>
            <tr class="b">
              <th><ww:text name="buildResult.buildNumber"/></th>
              <td>
                <ww:if test="buildResult.buildNumber != 0">
                    <ww:property value="buildResult.buildNumber"/>
                </ww:if>
                <ww:else>
                    &nbsp;
                </ww:else>
              </td>
            </tr>
          </table>
        </div>

        <ww:if test="buildResult.state == 4">
          <h4><ww:text name="buildResult.buildError"/></h4>
          <p>
            <div style="width:100%; height:500px; overflow:auto; border-style: solid; border-width: 1px">
            <code><pre><ww:property value="buildResult.error"/></pre></code>
            </div>
          </p>
        </ww:if>
        <ww:else>
          <h4><ww:text name="buildResult.buildOutput"/></h4>
          <p>
            <ww:if test="buildResult.output == ''">
                <ww:text name="buildResult.noOutput"/>
            </ww:if>
            <ww:else>
              <div style="width:100%; height:500px; overflow:auto; border-style: solid; border-width: 1px">
                <code><pre>buildResult.output</pre></code>
              </div>
            </ww:else>
          </p>
        </ww:else>
      </div>
    </body>
  </ww:i18n>
</html>
