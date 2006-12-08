<%@ taglib uri="/webwork" prefix="ww" %>
<%@ taglib uri="http://www.extremecomponents.org" prefix="ec" %>
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c' %>
<html>
<ww:i18n name="localization.Continuum">
  <head>
    <title><ww:text name="groups.page.title"/></title>
  </head>

  <body>
  <div id="h3">

    <ww:if test="${infoMessage != null}">
       <p>${infoMessage}</p>
    </ww:if>
    <ww:else>
       <h3>Project Groups</h3>
    </ww:else>
  
    <ww:if test="${empty groups}">
      No Project Groups Known.
    </ww:if>

    <ww:if test="${not empty groups}">
    <div class="eXtremeTable" >
    <ec:table items="groups"
              var="group"
              showExports="false"
              showPagination="false"
              showStatusBar="false"
              sortable="false"
              filterable="false">
      <ec:row highlightRow="true">
        <ec:column property="name" title="Name" width="20%" style="white-space: nowrap">
          <a href="<ww:url  action="projectGroupSummary" namespace="/"><ww:param name="projectGroupId" value="%{'${group.id}'}"/></ww:url>">${group.name}</a>
        </ec:column>
        <ec:column property="groupId" title="Group&nbsp;Id" width="20%"/>
        <ec:column property="numProjects" title="Projects" width="1%" style="text-align: center"/>
        <ec:column property="numSuccesses" title="Build&nbsp;Status" width="1%" style="white-space: nowrap" cell="org.apache.maven.continuum.web.view.BuildStatusCell"/>
      </ec:row>
    </ec:table>
    </ww:if>
    <pss:ifAuthorized permission="continuum-add-group">
      <div class="functnbar3">
        <table>
          <tr>
            <td>
              <form action="<ww:url  action='addProjectGroup' method='input' namespace='/' />" method="post">
                <input type="submit" name="addProjectGroup" value="<ww:text name="projectGroup.add.section.title"/>"/>
              </form>
            </td>
          </tr>
        </table>
      </div>
    </pss:ifAuthorized>
    </div>        
  </body>
</ww:i18n>
</html>
