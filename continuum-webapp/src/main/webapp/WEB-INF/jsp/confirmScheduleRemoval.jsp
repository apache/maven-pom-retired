<%@ taglib uri="/webwork" prefix="ww" %>
<%@ taglib uri="continuum" prefix="c1" %>
<html>
  <ww:i18n name="localization.Continuum">
    <head>
        <title>Schedule Removal</title>
    </head>
    <body>
      <div id="axial" class="h3">
        <h3>Schedule Removal</h3>
        <div class="axial">
        <ww:form action="removeSchedule" method="post">
          <ww:hidden name="id"/>
          <ww:hidden name="confirmed" value="true"/>
          <ww:actionerror/>

          <div class="warningmessage">
            <p>
              <strong>
                <ww:text name="schedules.confirmation.message">
                  <ww:param><ww:property value="%{name}"/></ww:param>
                </ww:text>
              </strong>
            </p>
          </div>

          <div class="functnbar3">
            <c1:submitcancel value="%{getText('delete')}" cancel="%{getText('cancel')}"/>
          </div>
        </ww:form>
        </div>
      </div>
    </body>
  </ww:i18n>
</html>
