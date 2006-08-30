<%@ taglib uri="/webwork" prefix="ww" %>
<%@ taglib uri="http://www.extremecomponents.org" prefix="ec" %>
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c' %>
<%@ taglib uri="continuum" prefix="c1" %>
<html>
<ww:i18n name="localization.Continuum">
  <head>
    <title>User Group Management</title>
  </head>

  <body>
  <div id="axial" class="h3">

     <div id="h3">
        <h3><ww:text name="User Groups"/></h3>

        <ec:table items="userGroups"
                  var="userGroup"
                  showExports="false"
                  showPagination="false"
                  showStatusBar="false"
                  filterable="false">
         <ec:row>
            <ec:column property="name" title="Name"/>
            <ec:column property="description" title="Description"/>
            <ec:column property="actions" title="&nbsp;">
                <ww:url id="editUrl" action="userGroup">
                  <ww:param name="userGroupId" value="${pageScope.userGroup.id}"/>
                </ww:url>
                <ww:url id="removeUrl" action="removeUserGroup">
                  <ww:param name="userGroupId" value="${pageScope.userGroup.id}"/>
                </ww:url>
                <ww:a href="%{editUrl}">Edit</ww:a>
                &nbsp;
                <ww:a href="%{removeUrl}">Delete</ww:a>
            </ec:column>
          </ec:row>
        </ec:table>
      </div>
      <div class="functnbar3">
        <ww:form action="userGroup" method="post">
          <ww:submit value="%{getText('add')}"/>
        </ww:form>
    </div>

  </div>
  </body>
</ww:i18n>
</html>
