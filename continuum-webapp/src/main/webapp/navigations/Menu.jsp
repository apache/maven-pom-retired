<%@ taglib uri="webwork" prefix="ww" %>
<div id="navcolum">
  <div id="projectmenu" class="toolgroup">
    <div class="label">Continuum</div>
    <div class="body">
      <div>
        <a href="<ww:url value="/about.jsp"/>">About</a>
      </div>
      <div>
        <a href="<ww:url value="/summary.action"/>">Show Projects</a>
      </div>
    </div>
    <div class="label">Add Project</div>
    <div class="body">
      <div>
        <a href="<ww:url value="addMavenTwoProject!default.action"/>">Maven 2.0.x Project</a>
      </div>
      <div>
        <a href="<ww:url value="addMavenOneProject!default.action"/>">Maven 1.x Project</a>
      </div>
      <div>
        <a href="<ww:url value="addProject!default.action"><ww:param name="projectType">ant</ww:param></ww:url>">Ant Project</a>
      </div>
      <div>
        <a href="<ww:url value="addProject!default.action"><ww:param name="projectType">shell</ww:param></ww:url>">Shell Project</a>
      </div>
    </div>
  </div>
  <div id="projectmenu" class="toolgroup">
    <div class="label">Administration</div>
    <div class="body">
      <div>
        <a href="<ww:url value="configuration!default.action"/>">Configuration</a>
      </div>
    </div>
  </div>
</div>