<%@ taglib uri="/webwork" prefix="ww" %>
<html>
  <ww:i18n name="localization.Continuum">
    <head>
        <title><ww:text name="error.page.title"/></title>
    </head>
    <body>
      <div id="axial" class="h3">
        <h3><ww:text name="error.section.title"/></h3>
        <p>
          <ww:text name="error.exception.message"/>
          <p>
           ${exception.message}
          </p>
        </p>
      </div>
    </body>
  </ww:i18n>
</html>
