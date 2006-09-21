<%@ taglib uri="/webwork" prefix="ww" %>
<%@ taglib uri="http://www.extremecomponents.org" prefix="ec" %>
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<%@ taglib uri="continuum" prefix="c1" %>
<html>
  <ww:i18n name="localization.Continuum">
    <head>
        <title><ww:text name="projectView.page.title"/></title>
    </head>
    <body>    
      <div id="h3">
        <ww:action name="projectGroupTab" executeResult="true">
            <ww:param name="tabName" value="'Summary'"/>
        </ww:action>
      
        <h3>Project Group Information</h3>
            
        <div class="axial">
          <table border="1" cellspacing="2" cellpadding="3" width="100%">
            <c1:data label="%{getText('projectView.project.name')}" name="projectGroup.name"/>
            <c1:data label="Group Id" name="projectGroup.groupId"/>
            <c1:data label="Description" name="projectGroup.description"/>
           </table>
        </div>

        <h3>Project Group Actions</h3>

        <div class="functnbar3">
          <ww:url id="buildProjectGroupUrl" action="buildProjectGroup">
            <ww:param name="projectGroupId" value="projectGroupId"/>
          </ww:url>
          <ww:url id="removeProjectGroupUrl" action="removeProjectGroup">
            <ww:param name="projectGroupId" value="projectGroupId"/>           
            <ww:param name="confirmed" value="false"/>
          </ww:url>
          <ww:a href="%{buildProjectGroupUrl}">Build</ww:a>&nbsp;<ww:a href="%{removeProjectGroupUrl}">Remove</ww:a>
        </div>

        <h3>Projects</h3>

        <ww:action name="projectSummary" executeResult="true" namespace="component">
          <ww:param name="projectGroupId" value="%{projectGroupId}"/>
        </ww:action>

      </div>
    </body>
  </ww:i18n>
</html>
