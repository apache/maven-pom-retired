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
        <title>Project Group Removal</title>
    </head>
    <body>
      <div id="axial" class="h3">
        <h3>Project Group Removal</h3>
        <div class="axial">
        <ww:form action="removeProjectGroup" method="post">
          <ww:hidden name="projectGroupId"/>
          <ww:hidden name="confirmed" value="true"/>
          <ww:actionerror/>

          <div class="warningmessage">
            <p>
              <strong>
                <ww:text name="groups.confirmation.message">
                  <ww:param><ww:property value="%{name}"/></ww:param>
                </ww:text>
              </strong>
            </p>
          </div>

          <div class="functnbar3">
            <c1:submitcancel value="%{getText('delete')}" cancel="%{getText('cancel')}"/>
          </div>
        </ww:form>
        </div>
      </div>
    </body>
  </ww:i18n>
</html>
