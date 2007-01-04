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
<%@ taglib uri="continuum" prefix="c1" %>
<html>
  <ww:i18n name="localization.Continuum">
    <head>
        <title><ww:text name="releaseProject.page.title"/></title>
    </head>
    <body>
      <h2><ww:text name="releaseViewResult.section.title"/></h2>

      <h4><ww:text name="releaseViewResult.summary"/></h4>
      <div class="axial">
        <table border="1" cellspacing="2" cellpadding="3" width="100%">
          <c1:data label="%{getText('releaseViewResult.startTime')}">
              <ww:param name="after"><c1:date name="result.startTime"/></ww:param>
          </c1:data>
          <c1:data label="%{getText('releaseViewResult.endTime')}">
              <ww:param name="after"><c1:date name="result.endTime"/></ww:param>
          </c1:data>
          <c1:data label="%{getText('releaseViewResult.state')}">
            <ww:param name="after">
              <ww:if test="result.resultCode == 0">
                <ww:text name="releaseViewResult.success"/>
              </ww:if>
              <ww:else>
                <ww:text name="releaseViewResult.error"/>
              </ww:else>
            </ww:param>
          </c1:data>
        </table>
      </div>

      <h4><ww:text name="releaseViewResult.output"/></h4>
      <p>
        <ww:if test="result.output == ''">
            <ww:text name="releaseViewResult.noOutput"/>
        </ww:if>
        <ww:else>
          <div style="width:100%; height:500px; overflow:auto; border-style: solid; border-width: 1px">
            <pre><ww:property value="result.output"/></pre>
          </div>
        </ww:else>
      </p>

    </body>
  </ww:i18n>
</html>
