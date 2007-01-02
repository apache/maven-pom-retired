<%@ taglib uri="/webwork" prefix="ww" %>
<%@ taglib uri="continuum" prefix="c1" %>
<html>
  <ww:i18n name="localization.Continuum">
    <head>
        <title>Project Group Removal</title>
    </head>
    <body>
      <div id="axial" class="h3">
        <h3>Project Group Removal</h3>
        <div class="axial">
        <ww:form action="removeProjectGroup" method="post">
          <ww:hidden name="projectGroupId"/>
          <ww:hidden name="confirmed" value="true"/>
          <ww:actionerror/>

          <div class="warningmessage">
            <p>
              <strong>
                <ww:text name="groups.confirmation.message">
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
