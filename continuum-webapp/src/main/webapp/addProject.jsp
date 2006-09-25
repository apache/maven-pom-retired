<%@ taglib uri="/webwork" prefix="ww" %>
<%@ taglib uri="continuum" prefix="c1" %>
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
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
                    <ww:form method="post" action="addProject" validate="true">
                        <c:if test="${!empty actionErrors}">
                          <div class="errormessage">
                            <c:forEach items="${actionErrors}" var="actionError">
                              <p><ww:text name="${actionError}"/></p>
                            </c:forEach>
                          </div>
                        </c:if>
                        <input type="hidden" name="projectType" value="<ww:property value="projectType"/>">
                        <table>
                          <tbody>
                            <ww:textfield label="%{getText('projectName.label')}" name="projectName" required="true">
                                <ww:param name="desc"><p><ww:text name="projectName.message"/></p></ww:param>
                            </ww:textfield>
                            <ww:textfield label="%{getText('projectVersion.label')}" name="projectVersion" required="true">
                                <ww:param name="desc"><p><ww:text name="projectVersion.message"/></p></ww:param>
                            </ww:textfield>
                            <ww:textfield label="%{getText('projectScmUrl.label')}" name="projectScmUrl" required="true">
                                <ww:param name="desc"><p><ww:text name="projectScmUrl.message"/></p></ww:param>
                            </ww:textfield>
                            <ww:textfield label="%{getText('projectScmUsername.label')}" name="projectScmUsername">
                                <ww:param name="desc"><p><ww:text name="projectScmUsername.message"/></p></ww:param>
                            </ww:textfield>
                            <ww:password label="%{getText('projectScmPassword.label')}" name="projectScmPassword">
                                <ww:param name="desc"><p><ww:text name="projectScmPassword.message"/></p></ww:param>
                            </ww:password>
                            <ww:textfield label="%{getText('projectScmTag.label')}" name="projectScmTag">
                                <ww:param name="desc"><p><ww:text name="projectScmTag.message"/></p></ww:param>
                            </ww:textfield>
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
