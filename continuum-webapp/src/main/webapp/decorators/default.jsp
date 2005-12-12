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
    <link rel="StyleSheet" href="dtree.css" type="text/css" />
    <script type="text/javascript" src="<%= request.getContextPath() %>dtree.js"></script>
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
          <div id="nav_items">
            * <a href="<ww:url value="updateProfile!default.action"/>">My Account</a><br/>

            <p/>

            <ww:form theme="simple" action="search">
                <ww:textfield label="Search" theme="simple" name="query" size="10"/>
                <ww:submit theme="simple" value="Go"/>
            </ww:form>


<%--            <ww:action name="categoryTree" executeResult="true"/>--%>
          </div>
        </td>
        <td valign="top">
          <decorator:body/>
        </td>
      </tr>
    </table>
  </body>
</html>