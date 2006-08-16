<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%@ taglib uri="continuum" prefix="c1" %>
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
                        <c:if test="${!empty actionErrors}">
                          <div class="errormessage">
                            <c:forEach items="${actionErrors}" var="actionError">
                              <p><ww:text name="${actionError}"/></p>
                            </c:forEach>
                          </div>
                        </c:if>
                        <table>
                          <tbody>
                            <ww:textfield label="%{getText('add.m2.project.m2PomUrl.label')}" name="m2PomUrl">
                                <ww:param name="desc"><p><ww:text name="add.m2.project.m2PomUrl.message"/></p></ww:param>
                            </ww:textfield>
                            <c1:data label="">
                                <ww:param name="after"><strong><ww:text name="or"/></strong></ww:param>
                            </c1:data>
                            <ww:file label="%{getText('add.m2.project.m2PomFile.label')}" name="m2PomFile">
                                <ww:param name="desc"><p><ww:text name="add.m2.project.m2PomFile.message"/></p></ww:param>
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
