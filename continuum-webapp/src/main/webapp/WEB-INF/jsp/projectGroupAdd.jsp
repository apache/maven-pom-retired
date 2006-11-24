<%@ taglib uri="/webwork" prefix="ww" %>
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<%@ taglib uri="continuum" prefix="c1" %>
<html>
  <ww:i18n name="localization.Continuum">
    <head>
        <title><ww:text name="projectGroup.add.page.title"/></title>
    </head>
    <body>
      <div id="axial" class="h3">
        <h3><ww:text name="projectGroup.add.section.title"/></h3>

        <div class="axial">
          <ww:url id="actionUrl" action="addProjectGroup" includeContext="false" />
          <ww:form action="%{actionUrl}" method="post" >
            <c:if test="${!empty actionErrors}">
              <div class="errormessage">
                <c:forEach items="${actionErrors}" var="actionError">
                  <p><ww:text name="${actionError}"/></p>
                </c:forEach>
              </div>
            </c:if>
            <table>
              <tbody>
                <ww:textfield label="%{getText('projectGroup.add.name.label')}" name="name"  required="true"/>
                <ww:textfield label="%{getText('projectGroup.add.groupId.label')}" name="groupId" required="true"/>
                <ww:textfield label="%{getText('projectGroup.add.description.label')}" name="description"/>
              </tbody>
            </table>
            <div class="functnbar3">
              <c1:submitcancel value="%{getText('save')}" cancel="%{getText('cancel')}"/>
            </div>
          </ww:form>
        </div>
      </div>
    </body>
  </ww:i18n>
</html>
