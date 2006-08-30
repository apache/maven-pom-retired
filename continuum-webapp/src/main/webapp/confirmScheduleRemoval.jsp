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

          <p>
            Are you sure you wish to remove <ww:property value="schedule.name"/>?
          </p>

          <div class="functnbar3">
            <c1:submitcancel value="%{getText('delete')}" cancel="%{getText('cancel')}"/>
          </div>
        </ww:form>
        </div>
      </div>
    </body>
  </ww:i18n>
</html>
