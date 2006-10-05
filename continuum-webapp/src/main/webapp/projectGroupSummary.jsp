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

        <jsp:include page="/navigations/ProjectGroupMenu.jsp">
          <jsp:param name="tab" value="summary"/>
        </jsp:include>

        <h3>Project Group Actions</h3>

        <div class="functnbar3">
            <table>
               <tr>
                    <td>
                        <form action="buildProjectGroup.action" method="post">
                            <input type="hidden" name="projectGroupId" value="<ww:property value="projectGroupId"/>"/>
                            <input type="submit" name="build" value="<ww:text name="Build"/>"/>
                        </form> 
                    </td>
                    <td>
                        <form action="removeProjectGroup.action" method="post">
                            <input type="hidden" name="projectGroupId" value="<ww:property value="projectGroupId"/>"/>
                            <input type="submit" name="remove" value="<ww:text name="Remove"/>"/>
                        </form>                    
                    </td>                    
                </tr>
            </table>
        </div>

        <ww:action name="projectSummary" executeResult="true" namespace="component">
          <ww:param name="projectGroupId" value="%{projectGroupId}"/>
        </ww:action>

      </div>
    </body>
  </ww:i18n>
</html>
