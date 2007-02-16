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

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<%@ taglib uri="/webwork" prefix="ww" %>

<ww:i18n name="localization.Continuum">
<html>
<head>
  <title>
    <c:choose>
      <c:when test="${param.errorCode == 403 || param.errorCode == 404 || param.errorCode == 500}">
        <ww:text name="error.${param.errorCode}.title"/>
      </c:when>
      <c:otherwise>
        <ww:text name="error.page.title"/>
      </c:otherwise>
    </c:choose>
  </title>
  <link rel="stylesheet" type="text/css" href="<ww:url value="/css/tigris.css"/>" media="screen"/>
  <link rel="stylesheet" type="text/css" href="<ww:url value="/css/print.css"/>" media="print"/>
  <link rel="stylesheet" type="text/css" href="<ww:url value="/css/extremecomponents.css"/>" media="screen"/>
  <link rel="shortcut icon" href="<ww:url value="/favicon.ico"/>" type="image/x-icon"/>

  <script src="<ww:url value="/scripts/tigris.js"/>" type="text/javascript"></script>
</head>

<body onload="focus()" marginwidth="0" marginheight="0" class="composite">
<%@ include file="/WEB-INF/jsp/navigations/DefaultTop.jsp" %>

<table id="main" border="0" cellpadding="4" cellspacing="0" width="100%">
  <tbody>
    <tr valign="top">
      <td id="leftcol" width="180">
        <br/> <br/>
        <%@ include file="/WEB-INF/jsp/navigations/Menu.jsp" %>
      </td>
      <td width="86%">
        <br/>

        <div id="bodycol">
          <div class="app">
            <div id="axial" class="h3">
              <h3>
                <c:choose>
                  <c:when test="${param.errorCode == 403 || param.errorCode == 404 || param.errorCode == 500}">
                    <ww:text name="error.${param.errorCode}.section.title"/>
                  </c:when>
                  <c:otherwise>
                    The URL requested results to an unknown error.
                  </c:otherwise>
                </c:choose>
              </h3>

              <div class="errormessage">
                <c:choose>
                  <c:when test="${param.errorCode == 403 || param.errorCode == 404 || param.errorCode == 500}">
                    <ww:text name="error.${param.errorCode}.message"/>
                  </c:when>
                  <c:otherwise>
                    The URL requested results to an unknown error.
                  </c:otherwise>
                </c:choose>
              </div>
            </div>
          </div>
        </div>
      </td>
    </tr>
  </tbody>
</table>

<%@ include file="/WEB-INF/jsp/navigations/DefaultBottom.jsp" %>
</body>
</html>
</ww:i18n>