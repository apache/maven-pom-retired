<%@ taglib uri="webwork" prefix="ww" %>
<html>
  <ww:i18n name="localization.Continuum">
    <head>
        <title><ww:text name="projectEdit.page.title"/></title>
    </head>
    <body>
      <div id="axial" class="h3">
        <h3><ww:text name="projectEdit.section.title"/></h3>

        <div class="axial">
          <ww:form action="projectEdit.action" method="post">
            <input type="hidden" name="projectId" value="<ww:property value="project.id"/>"/>
            <ww:textfield label="%{getText('projectEdit.project.name.label')}" name="project.name"/>
            <ww:textfield label="%{getText('projectEdit.project.version.label')}" name="project.version"/>
            <ww:textfield label="%{getText('projectEdit.project.scmUrl.label')}" name="project.scmUrl"/>
            <ww:textfield label="%{getText('projectEdit.project.scmUsername.label')}" name="project.scmUsername"/>
            <ww:password label="%{getText('projectEdit.project.scmPassword.label')}" name="project.scmPassword"/>
            <ww:textfield label="%{getText('projectEdit.project.scmTag.label')}" name="project.scmTag"/>
            <ww:submit value="%{getText('save')}"/>
          </ww:form>
        </div>
      </div>
    </body>
  </ww:i18n>
</html>
