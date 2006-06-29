<tr>
    <td colspan="2"><div <#rt/>
<#if parameters.align?exists>
    align="${parameters.align?html}"<#t/>
</#if>
><#t/>
<#if parameters.before?exists>
${parameters.before}
</#if>
<#include "/${parameters.templateDir}/simple/submit.ftl" />
<#if parameters.after?exists>
${parameters.after}
</#if>
</div><#t/>
<#include "/${parameters.templateDir}/${parameters.theme}/controlfooter.ftl" />
