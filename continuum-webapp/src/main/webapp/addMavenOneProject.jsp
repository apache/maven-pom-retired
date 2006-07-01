<%@ taglib uri="/webwork" prefix="ww" %>
<%@ taglib uri="continuum" prefix="c1" %>
<ww:i18n name="localization.Continuum">
<html>
    <head>
        <title><ww:text name="add.m1.project.page.title"/></title>
    </head>
    <body>
        <div class="app">
            <div id="axial" class="h3">
            <h3><ww:text name="add.m1.project.section.title"/></h3>
                <div class="axial">
                    <ww:form method="post" action="addMavenOneProject.action" name="addMavenOneProject" enctype="multipart/form-data">
                        <table>
                          <tbody>
                            <ww:textfield label="%{getText('add.m1.project.m1PomUrl.label')}" name="m1PomUrl">
                                <ww:param name="desc"><p><ww:text name="add.m1.project.m1PomUrl.message"/></p></ww:param>
                            </ww:textfield>
                            <c1:data label="">
                                <ww:param name="after"><strong><ww:text name="or"/></strong></ww:param>
                            </c1:data>
                            <ww:file label="%{getText('add.m1.project.m1PomFile.label')}" name="m1PomFile">
                                <ww:param name="desc"><p><ww:text name="add.m1.project.m1PomFile.message"/></p></ww:param>
                            </ww:file>
                          </tbody>
                        </table>
                        <div class="functnbar3">
                          <c1:submitcancel value="%{getText('add')}" cancel="%{getText('cancel')}"/>
                        </div>
                  </ww:form>
                </div>
            </div>
        </div>
    </body>
</html>
</ww:i18n>
