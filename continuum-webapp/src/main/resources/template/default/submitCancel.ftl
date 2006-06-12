<tr>
    <td colspan="2"><div <#rt/>
<#if parameters.align?exists>
    align="${parameters.align?html}"<#t/>
</#if>
><#t/>
<#include "/${parameters.templateDir}/simple/submit.ftl" />
<#if parameters.cancel?exists>
&nbsp;<input type="button" name="Cancel" value="${parameters.cancel}" onClick="history.back()"/>
</#if>
</div><#t/>
<#include "/${parameters.templateDir}/${parameters.theme}/controlfooter.ftl" />
