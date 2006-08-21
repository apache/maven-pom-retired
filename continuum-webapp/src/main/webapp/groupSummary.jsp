<%@ taglib uri="/webwork" prefix="ww" %>
<%@ taglib uri="/tld/extremecomponents" prefix="ec" %>
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c' %>
<html>
<ww:i18n name="localization.Continuum">
  <head>
    <title><ww:text name="groups.page.title"/></title>
  </head>

  <body>
  <div id="h3">

    <h3>Project Groups</h3>

    <p/>

    <ww:if test="${groups == null }">
      No Project Groups Known.
    </ww:if>

    <c:forEach items="${groups}" var="group">

      <ww:set name="group" value="${group}"/>
      <ww:url id="projectGroupSummaryUrl" action="projectGroupSummary" namespace="/">
        <ww:param name="projectGroupId" value="%{'${group.id}'}"/>
      </ww:url>

      <table border="2" cellspacing="2" cellpadding="3" width="100%">
        <tr>
          <td>Name:</td>
          <td><ww:a href="%{projectGroupSummaryUrl}">${group.name}</ww:a></td>
        </tr>
        <tr>
          <td>Group Id:</td>
          <td>${group.groupId}</td>
        </tr>
        <tr>
          <td>Project Type:</td>
          <td><- Maven2/Maven1/Ant/Shell -></td>
        </tr>
        <tr>
          <td>Number of Projects:</td>
          <td><- 15 -></td>
        </tr>
        <tr>
          <td>Build Status:</td>
          <td>
            <table>
              <tr>
                <td><img src="<ww:url value="/images/icon_success_sml.gif"/>" alt="<ww:text name="message.success"/>"
                         title="<ww:text name="message.success"/>"/></td>
                <td><ww:property value="${group.numSuccesses}"/></td>
              </tr>
              <tr>
                <td><img src="<ww:url value="/images/icon_warning_sml.gif"/>" alt="<ww:text name="message.failed"/>"
                         title="<ww:text name="message.failed"/>"/></td>
                <td><ww:property value="${group.numFailures}"/></td>
              </tr>
              <tr>
                <td><img src="<ww:url value="/images/icon_error_sml.gif"/>" alt="<ww:text name="message.error"/>"
                         title="<ww:text name="message.error"/>"/></td>
                <td><ww:property value="${group.numErrors}"/></td>
              </tr>
            </table>
          </td>
        </tr>
        <tr>
          <td>Next Scheduled Build:</td>
          <td><- timestamp -></td>
        </tr>
        <tr>
          <td>Status Message:</td>
          <td><- currently building Module X -></td>
        </tr>
      </table>
      <hr/>
      <p/>
    </c:forEach>
  </div>
  </body>
</ww:i18n>
</html>
