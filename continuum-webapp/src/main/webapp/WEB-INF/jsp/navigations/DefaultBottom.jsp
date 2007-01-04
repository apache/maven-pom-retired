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
<%@ page import="java.util.Calendar" %>
<ww:i18n name="localization.Continuum">
<%
  int inceptionYear = 2005;
  int currentYear = Calendar.getInstance().get( Calendar.YEAR );
  String copyrightRange = String.valueOf( inceptionYear );
  if ( inceptionYear != currentYear )
  {
    copyrightRange = copyrightRange + "-" + String.valueOf( currentYear );
  }
%>
<div id="footer">
  <div class="xright">
    Copyright &copy; <%= copyrightRange %> Apache Software Foundation
  </div>

  <div class="clear">
    <hr/>

  </div>
</div>
</ww:i18n>