<#include "/${parameters.templateDir}/${parameters.theme}/form-validate.ftl" />
<#include "/${parameters.templateDir}/simple/form.ftl" />
<table border="1" cellspacing="2" cellpadding="3" width="100%"<#rt/>
<#if parameters.cssStyle?exists> class="${parameters.cssClass?html}"<#rt/>
</#if>
<#if parameters.cssStyle?exists> style="${parameters.cssStyle?html}"<#rt/>
</#if>
>
  <tbody>
