<%@ taglib uri="/webwork" prefix="ww" %>
<%@ taglib uri="http://www.extremecomponents.org" prefix="ec" %>
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c' %>
<%@ taglib uri="continuum" prefix="c1" %>
<html>
  <ww:i18n name="localization.Continuum">
    <head>
      <title><ww:text name="projectGroup.page.title"/></title>
    </head>

    <body>
      <div id="h3">

        <ww:action name="projectGroupTab" executeResult="true">
          <ww:param name="tabName" value="'Notifier'"/>
        </ww:action>
    
        <ww:action name="projectGroupNotifierSummary" executeResult="true" namespace="component">
          <ww:param name="projectGroupId" value="%{projectGroupId}"/>
          <ww:param name="projectGroupName" value="%{projectGroup.name}"/>
        </ww:action>
      </div>
    </body>
  </ww:i18n>
</html>
