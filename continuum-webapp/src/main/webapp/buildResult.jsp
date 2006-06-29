<%@ taglib uri="/webwork" prefix="ww" %>
<%@ taglib uri="/tld/extremecomponents" prefix="ec" %>
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<%@ taglib prefix="c1" uri="continuum" %>
<html>
  <ww:i18n name="localization.Continuum">
    <head>
        <title><ww:text name="buildResult.page.title"/></title>
    </head>
    <body>
      <div id="h3">
        <div>
          <p style="border-top: 1px solid transparent; border-bottom: 1px solid #DFDEDE;">
            <a style="border: 1px solid #DFDEDE; padding-left: 1em; padding-right: 1em; text-decoration: none;" href="<ww:url value="/projectView.action?projectId="/><ww:property value="projectId"/>&projectName=<ww:property value="projectName"/>"><ww:text name="info"/></a>
            <a style="border: 1px solid #DFDEDE; padding-left: 1em; padding-right: 1em; text-decoration: none;" href="<ww:url value="/buildResults.action?projectId="/><ww:property value="projectId"/>&projectName=<ww:property value="projectName"/>"><ww:text name="builds"/></a>
            <a style="border: 1px solid #DFDEDE; padding-left: 1em; padding-right: 1em; text-decoration: none;" href="<ww:url value="/workingCopy.action?projectId="/><ww:property value="projectId"/>&projectName=<ww:property value="projectName"/>"><ww:text name="workingCopy"/></a>
          </p>
        </div>

        <h3>
            <ww:text name="buildResult.section.title">
                <ww:param><ww:property value="projectName"/></ww:param>
            </ww:text>
        </h3>

        <div class="axial">
          <table border="1" cellspacing="2" cellpadding="3" width="100%">
            <c1:data label="%{getText('buildResult.startTime')}">
                <ww:param name="after"><c1:date name="buildResult.startTime"/></ww:param>
            </c1:data>
            <c1:data label="%{getText('buildResult.endTime')}">
                <ww:param name="after"><c1:date name="buildResult.endTime"/></ww:param>
            </c1:data>
            <c1:data label="%{getText('buildResult.buildTrigger')}">
                <ww:param name="after">TODO<ww:property value="buildResult.trigger"/></ww:param>
            </c1:data>
            <c1:data label="%{getText('buildResult.state')}">
                <ww:param name="after">TODO<ww:property value="buildResult.state"/></ww:param>
            </c1:data>
            <c1:data label="%{getText('buildResult.buildNumber')}">
                <ww:param name="after">
                    <ww:if test="buildResult.buildNumber != 0">
                        <ww:property value="buildResult.buildNumber"/>
                    </ww:if>
                    <ww:else>
                        &nbsp;
                    </ww:else>
                </ww:param>
            </c1:data>
          </table>
        </div>

        <h4><ww:text name="buildResult.changes"/></h4>
        <ww:if test="buildResult.scmResult.changes != null && buildResult.scmResult.changes.size() > 0">
            <ww:set name="changes" value="buildResult.scmResult.changes" scope="request"/>
            <ec:table items="changes"
                      var="change"
                      showExports="false"
                      showPagination="false"
                      showStatusBar="false"
                      filterable="false">
              <ec:row>
                <ec:column property="author" title="buildResult.changes.author"/>
                <ec:column property="date" title="buildResult.changes.date" cell="date"/>
                <ec:column property="comment" title="buildResult.changes.comment" sortable="false"/>
                <ec:column property="files" title="buildResult.changes.files" sortable="false">
                    <c:forEach var="scmFile" items="${pageScope.change.files}">
                        <c:out value="${scmFile.name}"/><br />
                    </c:forEach>
                </ec:column>
              </ec:row>
            </ec:table>
        </ww:if>
        <ww:else>
          <b><ww:text name="buildResult.noChanges"/></b>
        </ww:else>

        <ww:if test="changesSinceLastSuccess != null && changesSinceLastSuccess.size() > 0">
            <h4><ww:text name="buildResult.changesSinceLastSuccess"/></h4>
            <ww:set name="changes" value="changesSinceLastSuccess" scope="request"/>
            <ec:table items="changes"
                      var="change"
                      showExports="false"
                      showPagination="false"
                      showStatusBar="false"
                      filterable="false">
              <ec:row>
                <ec:column property="author" title="buildResult.changes.author"/>
                <ec:column property="date" title="buildResult.changes.date" cell="date"/>
                <ec:column property="comment" title="buildResult.changes.comment" sortable="false"/>
                <ec:column property="files" title="buildResult.changes.files" sortable="false">
                    <c:forEach var="scmFile" items="${pageScope.change.files}">
                        <c:out value="${scmFile.name}"/><br />
                    </c:forEach>
                </ec:column>
              </ec:row>
            </ec:table>
        </ww:if>

        <ww:if test="buildResult.state == 4">
          <h4><ww:text name="buildResult.buildError"/></h4>
          <p>
            <div style="width:100%; height:500px; overflow:auto; border-style: solid; border-width: 1px">
            <code><pre><ww:property value="buildResult.error"/></pre></code>
            </div>
          </p>
        </ww:if>
        <ww:else>
          <h4><ww:text name="buildResult.buildOutput"/></h4>
          <p>
            <ww:if test="buildResult.output == ''">
                <ww:text name="buildResult.noOutput"/>
            </ww:if>
            <ww:else>
              <div style="width:100%; height:500px; overflow:auto; border-style: solid; border-width: 1px">
                <code><pre>buildResult.output</pre></code>
              </div>
            </ww:else>
          </p>
        </ww:else>
      </div>
    </body>
  </ww:i18n>
</html>
