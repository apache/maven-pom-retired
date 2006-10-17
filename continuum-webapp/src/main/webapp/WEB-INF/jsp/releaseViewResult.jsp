<%@ taglib uri="/webwork" prefix="ww" %>
<%@ taglib uri="continuum" prefix="c1" %>
<html>
  <ww:i18n name="localization.Continuum">
    <head>
        <title><ww:text name="release.page.title"/></title>
    </head>
    <body>
      <h2><ww:text name="releaseViewResult.section.title"/></h2>

      <h4><ww:text name="releaseViewResult.summary"/></h4>
      <div class="axial">
        <table border="1" cellspacing="2" cellpadding="3" width="100%">
          <c1:data label="%{getText('releaseViewResult.startTime')}">
              <ww:param name="after"><c1:date name="result.startTime"/></ww:param>
          </c1:data>
          <c1:data label="%{getText('releaseViewResult.endTime')}">
              <ww:param name="after"><c1:date name="result.endTime"/></ww:param>
          </c1:data>
          <c1:data label="%{getText('releaseViewResult.state')}">
            <ww:param name="after">
              <ww:if test="result.resultCode == 0">
                <ww:text name="releaseViewResult.success"/>
              </ww:if>
              <ww:else>
                <ww:text name="releaseViewResult.error"/>
              </ww:else>
            </ww:param>
          </c1:data>
        </table>
      </div>

      <h4><ww:text name="releaseViewResult.output"/></h4>
      <p>
        <ww:if test="result.output == ''">
            <ww:text name="releaseViewResult.noOutput"/>
        </ww:if>
        <ww:else>
          <div style="width:100%; height:500px; overflow:auto; border-style: solid; border-width: 1px">
            <pre><ww:property value="result.output"/></pre>
          </div>
        </ww:else>
      </p>

    </body>
  </ww:i18n>
</html>
