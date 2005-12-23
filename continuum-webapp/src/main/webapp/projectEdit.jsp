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
            <ww:textfield label="%{getText('projectEdit.project.name.label')}" name="name"/>
            <ww:textfield label="%{getText('projectEdit.project.version.label')}" name="version"/>
            <ww:textfield label="%{getText('projectEdit.project.scmUrl.label')}" name="scmUrl"/>
            <ww:textfield label="%{getText('projectEdit.project.scmUsername.label')}" name="scmUsername"/>
            <ww:password label="%{getText('projectEdit.project.scmPassword.label')}" name="scmPassword"/>
            <ww:textfield label="%{getText('projectEdit.project.scmTag.label')}" name="scmTag"/>
            <ww:submit value="%{getText('save')}"/>
          </ww:form>
        </div>
      </div>
    </body>
  </ww:i18n>
</html>
