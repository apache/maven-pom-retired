<%@ taglib uri="/webwork" prefix="ww" %>
<html>
  <ww:i18n name="localization.Continuum">
<head>
<title><ww:text name="editSchedule.page.title"/></title>              
</head>
<body>
<div class="app">
  <div id="axial" class="h3">
    <h3><ww:text name="editSchedule.page.title"/></h3>

    <div class="axial">
      <ww:form action="editSchedule.action" method="post">
        <input type="hidden" name="id" value="<ww:property value="id"/>"/>
        <ww:textfield label="%{getText('schedule.name.label')}" name="name" required="true">
            <ww:param name="desc"><p><ww:text name="schedule.name.message"/></p></ww:param>
        </ww:textfield>
        <ww:textfield label="%{getText('schedule.description.label')}" name="description" required="true">
            <ww:param name="desc"><p><ww:text name="schedule.description.message"/></p></ww:param>
        </ww:textfield>
        <ww:textfield label="%{getText('schedule.cronExpression.label')}" name="cronExpression" required="true">
            <ww:param name="desc"><p><ww:text name="schedule.cronExpression.message"/></p></ww:param>
        </ww:textfield>
        <ww:textfield label="%{getText('schedule.quietPeriod.label')}" name="delay">
            <ww:param name="desc"><p><ww:text name="schedule.quietPeriod.message"/></p></ww:param>
        </ww:textfield>
        <ww:checkbox label="%{getText('schedule.enabled.label')}" name="active" value="active" fieldValue="true">
            <ww:param name="desc"><p><ww:text name="schedule.enabled.message"/></p></ww:param>
        </ww:checkbox>
        <c1:submitcancel value="%{getText('save')}" cancel="%{getText('cancel')}"/>
      </ww:form>
    </div>
  </div>
</div>

</body>
</ww:i18n>
</html>