<%@ taglib uri="/webwork" prefix="ww" %>
<ww:i18n name="localization.Continuum">
  <ww:if test="companyLogo != null && companyLogo != ''">
    <ww:if test="companyUrl != null && companyUrl != ''">
        <a href="<ww:property value="companyUrl"/>">
    </ww:if>
        <img src="<ww:property value="companyLogo"/>" alt="<ww:property value="companyName"/>" title="<ww:property value="companyName"/>" border="0"/>
    <ww:if test="companyUrl != null && companyUrl != ''">
        </a>
    </ww:if>
  </ww:if>
  <ww:else>
    <a href="http://maven.apache.org/continuum/"><img src="<ww:url value="/images/asf_logo_wide.gif"/>" alt="Apache Software Foundation" title="Apache Software Foundation" border="0"></a>
  </ww:else>
</ww:i18n>
