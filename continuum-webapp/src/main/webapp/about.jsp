<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="continuum" prefix="c1" %>
<html>
  <ww:i18n name="localization.Continuum">
    <head>
        <title><ww:text name="about.page.title"/></title>
    </head>
    <body>
      <div id="axial" class="h3">
        <h3><ww:text name="about.section.title"/></h3>
        <div class="axial">
          <table border="1" cellspacing="2" cellpadding="3" width="100%">
            <c1:data label="%{getText('about.version.label')}">
                <ww:param name="after"><ww:text name="about.version.number"/></ww:param>
            </c1:data>
          </table>
        </div>
      </div>
    </body>
  </ww:i18n>
</html>
