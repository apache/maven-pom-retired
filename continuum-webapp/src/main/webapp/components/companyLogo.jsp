<%@ taglib uri="webwork" prefix="ww" %>
<ww:i18n name="localization.Continuum">

<ww:if test="companyLogo != null && companyLogo != ''">
    <ww:if test="companyUrl != null && companyUrl != ''">
        <a href="<ww:property value="companyUrl"/>">
    </ww:if>
        <img src="<ww:property value="companyLogo"/>" alt="<ww:property value="companyName"/>" title="<ww:property value="companyName"/>"/>
    <ww:if test="companyUrl != null && companyUrl != ''">
        </a>
    </ww:if>
</ww:if>
<ww:else>
    <a href="http://maven.apache.org/continuum/"><ww:text name="top.logo.default"/></a>
</ww:else>
</ww:i18n>
