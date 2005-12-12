<%@ taglib uri="sitemesh-decorator" prefix="decorator" %>
<%@ taglib uri="webwork" prefix="ww" %>
<html>
  <head>
    <title><decorator:title/></title>
    <style type="text/css">
    <!--
    @import url("<ww:url value="/shared/styles/main.css"/>");
    -->
    </style>
    <decorator:head/>
  </head>

  <body>
    <table>
      <tr>
        <td colspan="2">
          <img src="<ww:url value="/shared/images/logo.png"/> "/>
        </td>
      </tr>
      <tr>
        <td valign="top">
          &nbsp;
        </td>
        <td valign="top">
          <decorator:body/>
        </td>
      </tr>
    </table>
  </body>
</html>