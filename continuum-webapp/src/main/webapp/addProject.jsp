<%@ taglib uri="webwork" prefix="ww" %>
<ww:i18n name="localization.Continuum">
<html>
    <head>
        <title>
        <ww:if test="projectType == \"shell\"">
            <ww:text name="add.shell.project.page.title"/>
        </ww:if>
        <ww:else>
            <ww:text name="add.ant.project.page.title"/>
        </ww:else>
        </title>
    </head>
    <body>              
        <div class="app">
            <div id="axial" class="h3">
                <h3>
                    <ww:if test="projectType == \"shell\"">
                        <ww:text name="add.shell.project.section.title"/>
                    </ww:if>
                    <ww:else>
                        <ww:text name="add.ant.project.section.title"/>
                    </ww:else>
                </h3>
                <div class="axial">
                    <ww:form method="post" action="addProject.action">
                        <input type="hidden" name="projectType" value="<ww:property value="projectType"/>">
                        <table border="1" cellspacing="2" cellpadding="3" width="100%">
                        <tr class="b">
                            <th><ww:text name="projectName.label"/></th>
                            <td >
                                <input type="text" name="projectName" value="" size="100"/>
                                <p><ww:text name="projectName.message"/></p>
                            </td>
                        </tr>
                        <tr class="b">
                            <th><ww:text name="projectVersion.label"/></th>
                            <td >
                                <input type="text" name="projectVersion" value="" size="100"/>
                                <p><ww:text name="projectVersion.message"/></p>
                             </td>
                        </tr>
                        <tr class="b">
                            <th><ww:text name="projectScmUrl.label"/></th>
                            <td >
                                <input type="text" name="projectScmUrl" value="" size="100"/>
                                <p><ww:text name="projectScmUrl.message"/></p>
                             </td>
                        </tr>
                        <tr class="b">
                            <th><ww:text name="projectScmUsername.label"/></th>
                            <td >
                                <input type="text" name="projectScmUsername" value="" size="100"/>
                                <p><ww:text name="projectScmUsername.message"/></p>
                            </td>
                        </tr>
                        <tr class="b">
                            <th><ww:text name="projectScmPassword.label"/></th>
                            <td >
                                <input type="password" name="projectScmPassword" value="" size="100"/>
                                <p><ww:text name="projectScmPassword.message"/></p>
                            </td>
                        </tr>
                        <tr class="b">
                            <th><ww:text name="projectScmTag.label"/></th>
                            <td >
                                <input type="text" name="projectScmTag" value="" size="100"/>
                                <p><ww:text name="projectScmTag.message"/></p>
                            </td>
                        </tr>                                                                                                                
                        </table>
                        <div class="functnbar3">
                            <input type="submit" value="<ww:text name="add"/>"/>
                            <input type="button" value="<ww:text name="cancel"/>" onClick="history.back()"/>
                        </div>
                    </ww:form>
                </div>
            </div>
        </div>
    </body>
</html>
</ww:i18n>
