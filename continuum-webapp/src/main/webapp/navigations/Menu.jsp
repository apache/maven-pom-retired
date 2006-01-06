<%@ taglib uri="webwork" prefix="ww" %>
<ww:i18n name="localization.Continuum">
<div id="navcolum">
  <div id="projectmenu" class="toolgroup">
    <div class="label">Continuum</div>
    <div class="body">
      <div>
        <a href="<ww:url value="/about.jsp"/>"><ww:text name="menu.continuum.about"/></a>
      </div>
      <div>
        <a href="<ww:url value="/summary.action"/>"><ww:text name="menu.continuum.showProjects"/></a>
      </div>
    </div>
  </div>
  <div id="projectmenu" class="toolgroup">
    <div class="label"><ww:text name="menu.addProject"/></div>
    <div class="body">
      <div>
        <a href="<ww:url value="addMavenTwoProject!default.action"/>"><ww:text name="menu.add.m2Project"/></a>
      </div>
      <div>
        <a href="<ww:url value="addMavenOneProject!default.action"/>"><ww:text name="menu.add.m1Project"/></a>
      </div>
      <div>
        <a href="<ww:url value="addProject!default.action"><ww:param name="projectType">ant</ww:param></ww:url>"><ww:text name="menu.add.antProject"/></a>
      </div>
      <div>
        <a href="<ww:url value="addProject!default.action"><ww:param name="projectType">shell</ww:param></ww:url>"><ww:text name="menu.add.shellProject"/></a>
      </div>
    </div>
  </div>
  <div id="projectmenu" class="toolgroup">
    <div class="label"><ww:text name="menu.administration"/></div>
    <div class="body">
      <div>
        <a href="<ww:url value="schedules.action"/>"><ww:text name="menu.administration.schedules"/></a>
      </div>
      <div>
        <a href="<ww:url value="configuration!default.action"/>"><ww:text name="menu.administration.configuration"/></a>
      </div>
    </div>
  </div>
</div>
</ww:i18n>
