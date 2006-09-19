<%--
 * Copyright 2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
--%>


<%@ taglib uri="/webwork" prefix="ww" %>
<%@ taglib uri="continuum" prefix="c1" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
  <ww:i18n name="localization.Continuum">
    <head>
        <title>${confirmationTitle}</title>
    </head>
    <body>
      <div id="axial" class="h3">
        <h3>${confirmationTitle}</h3>
        <div class="axial">
        <ww:form action="${action}" method="post">
          <ww:hidden name="${confirmedName}" value="${confirmedValue}"/>
          <ww:hidden name="confirmed" value="true"/>
          <ww:actionerror/>

          <p>
            Are you sure you wish to remove <c:out value="${confirmedDisplay}"/>?
          </p>

          <div class="functnbar3">
            <c1:submitcancel value="%{getText('delete')}" cancel="%{getText('cancel')}"/>
          </div>
        </ww:form>
        </div>
      </div>
    </body>
  </ww:i18n>
</html>
