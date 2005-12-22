<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="/tld/extremecomponents" prefix="ec" %>
<html>
  <ww:i18n name="localization.Continuum">
    <head>
        <title><ww:text name="projectView.page.title"/></title>
    </head>
    <body>
      <div id="h3">
        <h3><ww:text name="projectView.section.title"/></h3>
            
        <div class="axial">
          <table border="1" cellspacing="2" cellpadding="3" width="100%">
            <tr class="b">
              <th><ww:text name="projectView.project.name"/></th>
              <td><ww:property value="project.name"/></td>
            </tr>
            <tr class="b">
              <th><ww:text name="projectView.project.version"/></th>
              <td><ww:property value="project.version"/></td>
            </tr>
            <tr class="b">
              <th><ww:text name="projectView.project.scmUrl"/></th>
              <td><ww:property value="project.scmUrl"/></td>
            </tr>
            <tr class="b">
              <th><ww:text name="projectView.project.group"/></th>
              <td><ww:property value="project.projectGroup.name"/></td>
            </tr>
          </table>
        </div>

        <h3><ww:text name="projectView.buildDefinitions"/></h3>
        <ec:table items="buildDefinitions"
                  var="buildDefinition"
                  showExports="false"
                  showPagination="false"
                  showStatusBar="false"
                  filterable="false">
          <ec:row highlightRow="true">
            <ec:column property="goals" title="projectView.buildDefinition.goals"/>
            <ec:column property="arguments" title="projectView.buildDefinition.arguments"/>
            <ec:column property="buildFile" title="projectView.buildDefinition.buildFile"/>
            <ec:column property="profile" title="projectView.buildDefinition.profile"/>
            <ec:column property="schedule" title="schedule">
                ${pageScope.buildDefinition.schedule.name}
            </ec:column>
            <ec:column property="from" title="projectView.buildDefinition.from">
                PROJECT
            </ec:column>
            <ec:column property="actions" title="&nbsp;">
                Edit&nbsp;Delete
            </ec:column>
          </ec:row>
        </ec:table>

        <h3><ww:text name="projectView.notifiers"/></h3>

        <h3><ww:text name="projectView.dependencies"/></h3>

        <h3><ww:text name="projectView.developers"/></h3>
        <ec:table items="developers"
                  showExports="false"
                  showPagination="false"
                  showStatusBar="false"
                  filterable="false">
          <ec:row highlightRow="true">
            <ec:column property="name" title="projectView.developer.name"/>
            <ec:column property="email" title="projectView.developer.email"/>
          </ec:row>
        </ec:table>

      </div>
    </body>
  </ww:i18n>
</html>
