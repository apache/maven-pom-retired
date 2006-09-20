<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
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
                        <c:if test="${!empty actionErrors}">
                          <div class="errormessage">
                            <c:forEach items="${actionErrors}" var="actionError">
                              <p><ww:text name="${actionError}"/></p>
                            </c:forEach>
                          </div>
                        </c:if>
                        <table>
                          <tbody>
                            <ww:textfield label="%{getText('add.m1.project.m1PomUrl.label')}" name="m1PomUrl">
                                <ww:param name="desc"><p><ww:text name="add.m1.project.m1PomUrl.message"/></p></ww:param>
                            </ww:textfield>
                            <ww:label>
                              <ww:param name="after"><strong><ww:text name="or"/></strong></ww:param>
                            </ww:label>
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
