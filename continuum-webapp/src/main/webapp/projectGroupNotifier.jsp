<%@ taglib uri="/webwork" prefix="ww" %>
<%@ taglib uri="http://www.extremecomponents.org" prefix="ec" %>
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c' %>
<%@ taglib uri="continuum" prefix="c1" %>
<html>
  <ww:i18n name="localization.Continuum">
    <head>
      <title><ww:text name="projectView.page.title"/></title>
    </head>

    <body>
      <div id="h3">

        <jsp:include page="/navigations/ProjectGroupMenu.jsp">
          <jsp:param name="tab" value="notifier"/>
        </jsp:include>
    
        <h3>PROJECT GROUP Notifiers<ww:text name="projectView.section.title"/></h3>
    
        <div class="axial">
          UNDER CONSTRUCTION
        </div>
      </div>
    </body>
  </ww:i18n>
</html>
