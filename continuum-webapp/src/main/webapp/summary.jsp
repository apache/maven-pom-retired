<%@ taglib uri="webwork" prefix="ww" %>
<html>
  <ww:i18n name="localization.Continuum">
    <head>
        <title><ww:text name="summary.page.title"/></title>
    </head>
    <body>
      <div id="h3">
        <h3><ww:text name="summary.section.title"/></h3>
        <table border="1" cellspacing="2" cellpadding="3" width="100%" id="projectSummaryTable">
    
          <!-- i18n -->
          <tbody><tr>
            <th>&nbsp;</th>
            <th width="100%"><ww:text name="summary.projectTable.name"/></th>
            <th><ww:text name="summary.projectTable.version"/></th>
            <th><ww:text name="summary.projectTable.build"/></th>
            <th><ww:text name="summary.projectTable.group"/></th>
            <th colspan="7"></th>
          </tr>
        </table>
        <div class="functnbar3">
          <img src="<ww:url value="/images/icon_success_sml.gif"/>" alt="<ww:text name="message.success"/>" title="<ww:text name="message.success"/>"/> <font color="red">TODO</font>
          <img src="<ww:url value="/images/icon_warning_sml.gif"/>" alt="<ww:text name="message.failed"/>" title="<ww:text name="message.failed"/>"/> <font color="red">TODO</font>
          <img src="<ww:url value="/images/icon_error_sml.gif"/>" alt="<ww:text name="message.error"/>" title="<ww:text name="message.error"/>"/> <font color="red">TODO</font>
        </div>
      </div>
    </body>
  </ww:i18n>
</html>