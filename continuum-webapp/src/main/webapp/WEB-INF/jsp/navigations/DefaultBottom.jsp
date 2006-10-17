<%@ taglib uri="/webwork" prefix="ww" %>
<%@ page import="java.util.Calendar" %>
<%
    int inceptionYear = 2005;
    int currentYear = Calendar.getInstance().get( Calendar.YEAR );
    String copyrightRange = String.valueOf( inceptionYear );
    if ( inceptionYear != currentYear )
    {
        copyrightRange = copyrightRange + "-" + String.valueOf( currentYear );
    }
%>
<ww:i18n name="localization.Continuum">
<div id="footer">
  <table with="100%" border="0" cellpadding="4" cellspacing="0">
    <tbody>
      <tr>
        <td>
          Continuum &#169; <%= copyrightRange %> Apache Software Foundation
        </td>
      </tr>
    </tbody>
  </table>
</div>
</ww:i18n>