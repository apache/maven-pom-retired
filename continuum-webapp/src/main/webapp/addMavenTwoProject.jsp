<%@ taglib uri="webwork" prefix="ww" %>
<ww:i18n name="localization.Continuum">
<html>
    <head>
        <title><ww:text name="add.m2.project.page.title"/></title>
    </head>
    <body>
        <div class="app">
            <div id="axial" class="h3">
                <h3><ww:text name="add.m2.project.section.title"/></h3>
                <div class="axial">
                    <ww:form method="post" action="addMavenTwoProject.action" name="addMavenTwoProject" enctype="multipart/form-data">
                        <table border="1" cellspacing="2" cellpadding="3" width="100%">
                        <tr class="b">
                            <th><ww:text name="add.m2.project.m2PomUrl.label"/></th>
                            <td >
                                <input type="text" name="m2PomUrl" value="" size="100"/>
                                <p><ww:text name="add.m2.project.m2PomUrl.message"/></p>
                            </td>
                        </tr>
                        <tr class="b">
                            <th>&nbsp;</th>
                            <td>
                                <strong>OR</strong>
                            </td>
                        </tr>
                        <tr class="b">
                            <th><ww:text name="add.m2.project.m2PomFile.label"/></th>
                            <td >
                                <input type="file" name="m2PomFile" value="" size="100"/>
                                <p><ww:text name="add.m2.project.m2PomFile.message"/></p>
                            </td>
                        </tr>
                        </table>
                        <div class="functnbar3">
                            <input type="submit" value="<ww:text name="delete.submit"/>"/>
                            <input type="button" name="Cancel" value="<ww:text name="cancel"/>" onClick="history.back()"/>
                        </div>
                    </ww:form>
                </div>
            </div>
        </div>
    </body>
</html>
</ww:i18n>
