<%@ taglib uri="webwork" prefix="ww" %>
<ww:i18n name="localization.Continuum">
<html>
    <head>
        <title><ww:text name="add.m1.project.page.title"/></title>
    </head>
    <body>
<div class="app">
    <div id="axial" class="h3">
    <h3><ww:text name="add.m1.project.section.title"/></h3>
    <ww:form method="post" action="addMavenOneProject.action" name="addMavenOneProject" enctype="multipart/form-data">
        <div class="axial">
        <table border="1" cellspacing="2" cellpadding="3" width="100%">
        <tr>
            <th><ww:text name="add.m1.project.m1PomUrl.label"/> </th>
            <td>
                <input type="text" name="m1PomUrl" value="" size="100"/>
                <p><ww:text name="add.m1.project.m1PomUrl.message"/></p>
            </td>
        </tr>
        <tr>
            <td>&nbsp;</td>
            <td>
                <strong>OR</strong>
            </td>
        </tr>
        <tr>
            <th><ww:text name="add.m1.project.m1PomFile.label"/></th>
            <td >
                <ww:file name="m1PomFile" value="" size="100"/>
                <p><ww:text name="add.m1.project.m1PomFile.message"/></p>
            </td>
        </tr>
        </table>
        <div class="functnbar3">
            <input type="submit" value="<ww:text name="delete.submit"/>"/>
            <input type="button" name="Cancel" value="<ww:text name="cancel"/>" onClick="history.back()"/>
        </div>
        </div>
    </ww:form>
    </div>
</div>
    </body>
</html>
</ww:i18n>
