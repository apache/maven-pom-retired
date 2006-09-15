<%@ taglib uri="/webwork" prefix="ww" %>
<%@ taglib uri="/tld/extremecomponents" prefix="ec" %>
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<%@ taglib prefix="c1" uri="continuum" %>
<html>
  <ww:i18n name="localization.Continuum">
    <head>
        <title><ww:text name="releaseResult.page.title"/></title>
    </head>
    <body>
      <div id="h3">
        <h3>
            <ww:text name="releaseResult.section.title">
                <ww:param><ww:property value="projectName"/></ww:param>
            </ww:text>
        </h3>

        <div class="axial">
          <table border="1" cellspacing="2" cellpadding="3" width="100%">
            <c1:data label="%{getText('releaseResult.startTime')}">
                <ww:param name="after"><c1:date name="releaseResult.startTime"/></ww:param>
            </c1:data>
            <c1:data label="%{getText('releaseResult.endTime')}">
                <ww:param name="after"><c1:date name="releaseResult.endTime"/></ww:param>
            </c1:data>
          </table>
        </div>

        <h4><ww:text name="releaseResult.buildError"/></h4>
        <p>
          <div style="width:100%; height:500px; overflow:auto; border-style: solid; border-width: 1px">
          <code><pre><ww:property value="releaseResult.error"/></pre></code>
          </div>
        </p>
      </div>
    </body>
  </ww:i18n>
</html>
