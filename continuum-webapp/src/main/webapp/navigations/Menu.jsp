<%@ taglib uri="/webwork" prefix="ww" %>
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c' %>
<%@ taglib uri="continuum" prefix="c1" %>
<ww:i18n name="localization.Continuum">
<div id="navcolum">
  <div id="projectmenu" class="toolgroup">
    <div class="label">Continuum</div>
    <div>
      <div class="body">
        <a href="<ww:url value="/about.jsp" includeParams="none"/>"><ww:text name="menu.continuum.about"/></a>
      </div>
      <div class="body">
        <a href="<ww:url value="/groupSummary.action" includeParams="none"/>"><ww:text name="menu.continuum.showProjectGroups"/></a>
      </div>
    </div>
  </div>

    <div id="projectmenu" class="toolgroup">
      <div class="label"><ww:text name="menu.addProject"/></div>
      <div>
        <div class="body">
          <a href="<ww:url value="/addMavenTwoProject!default.action" includeParams="none"/>"><ww:text name="menu.add.m2Project"/></a>
        </div>
        <div class="body">
          <a href="<ww:url value="/addMavenOneProject!default.action" includeParams="none"/>"><ww:text name="menu.add.m1Project"/></a>
        </div>
        <div class="body">
          <a href="<ww:url value="/addProject!default.action" includeParams="none"><ww:param name="projectType">ant</ww:param></ww:url>"><ww:text name="menu.add.antProject"/></a>
        </div>
        <div class="body">
          <a href="<ww:url value="/addProject!default.action" includeParams="none"><ww:param name="projectType">shell</ww:param></ww:url>"><ww:text name="menu.add.shellProject"/></a>
        </div>
      </div>
    </div>

    <div id="projectmenu" class="toolgroup">
      <div class="label"><ww:text name="menu.administration"/></div>
      <div>
      <c1:ifAuthorized permission="manageSchedule">
          <div class="body">
            <a href="<ww:url value="/schedules.action" includeParams="none"/>"><ww:text name="menu.administration.schedules"/></a>
          </div>
      </c1:ifAuthorized>
      <c1:ifAuthorized permission="manageConfiguration">
          <div class="body">
            <a href="<ww:url value="/configuration!default.action" includeParams="none"/>"><ww:text name="menu.administration.configuration"/></a>
          </div>
      </c1:ifAuthorized>
      <c1:ifAuthorized permission="manageUsers">
        <div class="body">
          <ww:url id="userManagementUrl" action="users"/>
          <ww:a href="%{userManagementUrl}">Users</ww:a>
        </div>
        <div class="body">
          <ww:url id="userGroupManagementUrl" action="userGroups"/>
          <ww:a href="%{userGroupManagementUrl}">User Groups</ww:a>
        </div>
      </c1:ifAuthorized>
      </div>
    </div>

  <div id="projectmenu" class="toolgroup">
    <div class="label">Legend</div>
    <div id="legend">
      <div id="litem1" class="body">Build Now</div>
      <div id="litem2" class="body">Build History</div>
      <div id="litem3" class="body">Build In Progess</div>
      <div id="litem4" class="body">Checking Out Build</div>
      <div id="litem5" class="body">Queued Build</div>
      <div id="litem6" class="body">Delete</div>
      <div id="litem7" class="body">Edit</div>
      <div id="litem8" class="body">Build in Success</div>
      <div id="litem9" class="body">Build in Failure</div>
      <div id="litem10" class="body">Build in Error</div>
    </div>
  </div>
</div>
</ww:i18n>
