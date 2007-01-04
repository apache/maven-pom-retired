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
    <title>
      <ww:text name="configuration.page.title"/>
    </title>
  </head>

  <body>
  <div id="axial" class="h3">
    <h3>
      <ww:text name="configuration.section.title"/>
    </h3>

    <ww:form action="configuration!save" method="post">

      <ww:if test="hasActionErrors()">
        <h3>Action Error</h3>
      </ww:if>
      <p>
        <ww:actionerror/>
      </p>

      <div class="axial">

        <table>
          <tbody>

            <ww:textfield label="%{getText('configuration.workingDirectory.label')}" name="workingDirectory"
                          required="true">
              <ww:param name="desc"><p>
                <ww:text name="configuration.workingDirectory.message"/>
              </p></ww:param>
            </ww:textfield>

            <ww:textfield label="%{getText('configuration.buildOutputDirectory.label')}" name="buildOutputDirectory"
                          required="true">
              <ww:param name="desc"><p>
                <ww:text name="configuration.buildOutputDirectory.message"/>
              </p></ww:param>
            </ww:textfield>

            <ww:textfield label="%{getText('configuration.deploymentRepositoryDirectory.label')}"
                          name="deploymentRepositoryDirectory">
              <ww:param name="desc"><p>
                <ww:text name="configuration.deploymentRepositoryDirectory.message"/>
              </p></ww:param>
            </ww:textfield>

            <ww:textfield label="%{getText('configuration.baseUrl.label')}" name="baseUrl" required="true">
              <ww:param name="desc"><p>
                <ww:text name="configuration.baseUrl.message"/>
              </p></ww:param>
            </ww:textfield>

          </tbody>
        </table>
        <div class="functnbar3">
          <c1:submitcancel value="%{getText('save')}" cancel="%{getText('cancel')}"/>
        </div>

      </div>
    </ww:form>
  </div>
  </body>
</ww:i18n>
</html>
