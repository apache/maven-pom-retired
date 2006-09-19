<%@ taglib uri="/webwork" prefix="ww" %>
<%@ taglib uri="http://www.extremecomponents.org" prefix="ec" %>
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<%@ taglib uri="continuum" prefix="c1" %>
<html>
<ww:i18n name="localization.Continuum">
  <head>
    <title>User Management</title>
  </head>

  <body>
  <div id="axial" class="h3">

     <div id="h3">
        <h3><ww:text name="Users"/></h3>

        <ec:table items="users"
                  var="user"
                  showExports="false"
                  showPagination="false"
                  showStatusBar="false"
                  filterable="false">
         <ec:row>
            <ec:column property="username" title="Name"/>
            <ec:column property="fullName" title="Full Name"/>
            <ec:column property="email" title="Email"/>
            <ec:column property="userGroupId" title="Group"/>
            <ec:column property="actions" title="&nbsp;">
                <ww:url id="editUrl" action="user">
                  <ww:param name="userId" value="${pageScope.user.accountId}"/>
                </ww:url>
                <ww:url id="removeUrl" action="removeUser">
                  <ww:param name="userId" value="${pageScope.user.accountId}"/>
                  <ww:param name="username" value="%{'${pageScope.user.username}'}"/>                  
                </ww:url>
                <ww:a href="%{editUrl}">Edit</ww:a>
                &nbsp;
                <ww:a href="%{removeUrl}">Delete</ww:a>
            </ec:column>
          </ec:row>
        </ec:table>
      </div>
      <div class="functnbar3">
        <ww:form action="user" method="post">
          <ww:submit value="%{getText('add')}"/>
        </ww:form>
    </div>

  </div>
  </body>
</ww:i18n>
</html>
