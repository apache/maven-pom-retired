<%--
  ~ Licensed to the Apache Software Foundation (ASF) under one
  ~ or more contributor license agreements.  See the NOTICE file
  ~ distributed with this work for additional information
  ~ regarding copyright ownership.  The ASF licenses this file
  ~ to you under the Apache License, Version 2.0 (the
  ~ "License"); you may not use this file except in compliance
  ~ with the License.  You may obtain a copy of the License at
  ~
  ~   http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied.  See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  --%>

<%@ taglib uri="/webwork" prefix="ww" %>
<%@ taglib uri="http://www.extremecomponents.org" prefix="ec" %>
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<%@ taglib prefix="c1" uri="continuum" %>
<html>
  <ww:i18n name="localization.Continuum">
    <head>
        <title><ww:text name="buildResult.page.title"/></title>
    </head>
    <body>
      <div id="h3">

        <jsp:include page="/WEB-INF/jsp/navigations/ProjectMenu.jsp"/>

        <h3>
            <ww:text name="buildResult.section.title">
                <ww:param><ww:property value="project.name"/></ww:param>
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
            <c1:data label="%{getText('buildResult.trigger')}">
                <ww:param name="after"><ww:text name="buildResult.trigger.%{buildResult.trigger}"/></ww:param>
            </c1:data>
            <c1:data label="%{getText('buildResult.state')}">
                <ww:param name="after" value="state"/>
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

        <h4><ww:text name="buildResult.scmResult.changes"/></h4>
        <ww:if test="buildResult.scmResult.changes != null && buildResult.scmResult.changes.size() > 0">
            <ww:set name="changes" value="buildResult.scmResult.changes" scope="request"/>
            <ec:table items="changes"
                      var="change"
                      showExports="false"
                      showPagination="false"
                      showStatusBar="false"
                      sortable="false"
                      filterable="false">
              <ec:row>
                <ec:column property="author" title="buildResult.scmResult.changes.author"/>
                <ec:column property="date" title="buildResult.scmResult.changes.date" cell="date"/>
                <ec:column property="comment" title="buildResult.scmResult.changes.comment" />
                <ec:column property="files" title="buildResult.scmResult.changes.files">
                    <c:forEach var="scmFile" items="${pageScope.change.files}">
                        <c:out value="${scmFile.name}"/><br />
                    </c:forEach>
                </ec:column>
              </ec:row>
            </ec:table>
        </ww:if>
        <ww:else>
          <b><ww:text name="buildResult.scmResult.noChanges"/></b>
        </ww:else>

        <ww:if test="changesSinceLastSuccess != null && changesSinceLastSuccess.size() > 0">
            <h4><ww:text name="buildResult.changesSinceLastSuccess"/></h4>
            <ww:set name="changes" value="changesSinceLastSuccess" scope="request"/>
            <ec:table items="changes"
                      var="change"
                      showExports="false"
                      showPagination="false"
                      showStatusBar="false"
                      sortable="false"
                      filterable="false">
              <ec:row>
                <ec:column property="author" title="buildResult.changes.author"/>
                <ec:column property="date" title="buildResult.changes.date" cell="date"/>
                <ec:column property="comment" title="buildResult.changes.comment" />
                <ec:column property="files" title="buildResult.changes.files">
                    <c:forEach var="scmFile" items="${pageScope.change.files}">
                        <c:out value="${scmFile.name}"/><br />
                    </c:forEach>
                </ec:column>
              </ec:row>
            </ec:table>
        </ww:if>

        <h4><ww:text name="buildResult.dependencies.changes"/></h4>
        <ww:if test="buildResult.modifiedDependencies != null && buildResult.modifiedDependencies.size() > 0">
            <ww:set name="dependencies" value="buildResult.modifiedDependencies" scope="request"/>
            <ec:table items="dependencies"
                      var="dep"
                      showExports="false"
                      showPagination="false"
                      showStatusBar="false"
                      sortable="false"
                      filterable="false">
              <ec:row>
                <ec:column property="groupId" title="buildResult.dependencies.groupId"/>
                <ec:column property="artifactId" title="buildResult.dependencies.artifactId"/>
                <ec:column property="version" title="buildResult.dependencies.version"/>
              </ec:row>
            </ec:table>
        </ww:if>
        <ww:else>
          <b><ww:text name="buildResult.dependencies.noChanges"/></b>
        </ww:else>

        <ww:if test="hasSurefireResults">
          <h4><ww:text name="buildResult.generatedReports.title"/></h4>

          <ww:url id="surefireReportUrl" action="surefireReport">
            <ww:param name="projectId" value="projectId"/>
            <ww:param name="buildId" value="buildId"/>
            <ww:param name="projectName" value="projectName"/>
          </ww:url>
          <ww:a href="%{surefireReportUrl}"><ww:text name="buildResult.generatedReports.surefire"/></ww:a>
        </ww:if>

        <ww:if test="buildResult.state == 4">
          <h4><ww:text name="buildResult.buildError"/></h4>
          <div style="width:100%; height:500px; overflow:auto; border-style: solid; border-width: 1px">
            <pre><ww:property value="buildResult.error"/></pre>
          </div>
        </ww:if>
        <ww:else>
          <h4><ww:text name="buildResult.buildOutput"/></h4>
          <p>
            <ww:if test="buildOutput == ''">
                <ww:text name="buildResult.noOutput"/>
            </ww:if>
            <ww:else>
              <div style="width:100%; height:500px; overflow:auto; border-style: solid; border-width: 1px">
                <pre><ww:property value="buildOutput"/></pre>
              </div>
            </ww:else>
          </p>
        </ww:else>
      </div>
    </body>
  </ww:i18n>
</html>
