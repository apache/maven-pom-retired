<%@ taglib uri="/webwork" prefix="ww" %>
<%@ page import="java.util.Calendar" %>
<ww:i18n name="localization.Continuum">
<%
  int inceptionYear = 2005;
  int currentYear = Calendar.getInstance().get( Calendar.YEAR );
  String copyrightRange = String.valueOf( inceptionYear );
  if ( inceptionYear != currentYear )
  {
    copyrightRange = copyrightRange + "-" + String.valueOf( currentYear );
  }
%>
<div id="footer">
  <div class="xright">
    Copyright &copy; <%= copyrightRange %> Apache Software Foundation
  </div>

  <div class="clear">
    <hr/>

  </div>
</div>
</ww:i18n>