<%@ taglib uri="webwork" prefix="ww" %>
<html>
  <ww:i18n name="localization.Continuum">
<head>
<title><ww:text name="editSchedule.page.title"/></title>              
</head>
<body>
<div class="app">
  <div id="axial" class="h3">
    <h3><ww:text name="editSchedule.page.title"/></h3>
    <form method="post" action="editSchedule.action">
      <input type="hidden" name="id" value="<ww:property value="id"/>"/>
      <div class="axial">
        <table border="1" cellspacing="2" cellpadding="3" width="100%">
          <tr>
            <th><ww:text name="schedule.name.label"/></th>
            <td >
            <input type="text" name="name" value="<ww:property value="name"/>" size="100"/>
            <p>
            <ww:text name="schedule.name.message"/>
            </p>
            </td>
          </tr>
          <tr>
            <th><ww:text name="schedule.description.label"/></th>
            <td >
            <input type="text" name="description" value="<ww:property value="description"/>" size="100"/>
            <p>
            <ww:text name="schedule.description.message"/>
            </p>
            </td>
          </tr>
          <tr>
            <th><ww:text name="schedule.cronExpression.label"/></th>
            <td >
            <input type="text" name="cronExpression" value="<ww:property value="cronExpression"/>" size="100"/>
            <p>
            <ww:text name="schedule.cronExpression.message"/>
            </p>
            </td>
          </tr>
          <tr>
            <th><ww:text name="schedule.delay.label"/></th>
            <td >
            <input type="text" name="delay" value="<ww:property value="delay"/>" size="100"/>
            <p>
            <ww:text name="schedule.delay.message"/>
            </p>
            </td>
          </tr>
          <tr>
            <th><ww:text name="schedule.enabled.label"/></th>
            <td >
            <input type="checkbox" name="active" value="true" <ww:if test="active">checked</ww:if> />
            <p>
            <ww:text name="schedule.enabled.message"/>
            </p>
            </td>
          </tr>
        </table>
        <div class="functnbar3">
          <input type="submit" value="Submit"/>
          <input type="button" value="Cancel" onClick="history.back()"/>
        </div>
      </div>
    </form>
  </div>
</div>

</body>
</ww:i18n>
</html>