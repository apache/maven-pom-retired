<#include "/${parameters.templateDir}/${parameters.theme}/controlheader.ftl" />
<#if parameters.valueLink?exists>
    <a href="${parameters.valueLink}">    
</#if>    
<#if parameters.nameValue?exists>${parameters.nameValue}</#if>
<#if parameters.valueLink?exists>
    </a>    
</#if>    
<#include "/${parameters.templateDir}/${parameters.theme}/controlfooter.ftl" />
