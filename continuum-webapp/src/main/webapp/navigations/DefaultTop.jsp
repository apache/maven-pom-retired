<%@ taglib uri="/webwork" prefix="ww" %>
<ww:i18n name="localization.Continuum">
<div id="banner">
  <table border="1" cellpadding="8" cellspacing="0" width="100%">
    <tbody>
      <tr>
        <td>
          <a href="http://maven.apache.org/continuum">
            <img src="<ww:url value="/images/continuum_logo_75.gif"/>" alt="Continuum" title="Continuum" border="0">
          </a>
        </td>
        <td>
          <div align="right">
            <ww:action name="companyInfo!default" executeResult="true"/>
          </div>
        </td>
      </tr>
    </tbody>
  </table>
</div>

<div id="breadcrumbs">
  <div style="float: right;">
    <a href="http://maven.apache.org/continuum">Continuum</a> |
    <a href="http://maven.apache.org/">Maven</a> |
    <a href="http://www.apache.org/">Apache</a>
  </div>

  <div>
      <b><font color="red">TODO</font></b>Welcome, <b>Guest</b> - <a href="<ww:url value="login!default.action"/>">Login</a>
  </div>
</div>
</ww:i18n>