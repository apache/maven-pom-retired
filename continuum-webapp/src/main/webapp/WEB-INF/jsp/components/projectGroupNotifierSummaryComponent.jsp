<%@ taglib uri="/webwork" prefix="ww" %>
<%@ taglib uri="http://www.extremecomponents.org" prefix="ec" %>
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<%@ taglib uri="continuum" prefix="c1" %>
<%@ taglib uri="/plexusSecuritySystem" prefix="pss" %>
<ww:i18n name="localization.Continuum">

  <h3>Project Group Notifiers</h3>
  <ec:table items="projectGroupNotifierSummaries"
            var="projectGroupNotifierSummary"
            showExports="false"
            showPagination="false"
            showStatusBar="false"
            filterable="false"
            sortable="false">
    <ec:row>
      <ec:column property="type" title="projectView.notifier.type"/>
      <ec:column property="recipient" title="projectView.notifier.recipient"/>
      <ec:column property="events" title="projectView.notifier.events"/>
      <ec:column property="sender" title="projectView.notifier.from"/>      
      <ec:column property="editActions" title="Edit">

        <!-- TODO: Fix WW action references for editing notifiers below -->
        <pss:ifAuthorized permission="continuum-modify-group" resource="${projectGroupName}">
          <ww:url id="editUrl" action="editNotifier" namespace="/">
            <ww:param name="projectGroupId">${pageScope.projectGroupNotifierSummary.projectGroupId}</ww:param>
            <ww:param name="notifierId">${pageScope.projectGroupNotifierSummary.id}</ww:param>
            <ww:param name="notifierType">${pageScope.projectGroupNotifierSummary.type}</ww:param>
          </ww:url>
          <ww:a href="%{editUrl}">
              <img src="<ww:url value='/images/edit.gif'/>" alt="Edit" title="Edit" border="0">
          </ww:a>
        </pss:ifAuthorized>
        <pss:elseAuthorized>
          <img src="<ww:url value='/images/edit_disabled.gif'/>" alt="Edit" title="Edit" border="0">
        </pss:elseAuthorized>
      </ec:column>    
      <ec:column property="deleteActions" title="Remove">
        <pss:ifAuthorized permission="continuum-modify-group" resource="${projectGroupName}">
          <ww:url id="removeUrl" action="deleteNotifier" namespace="/">
            <ww:param name="projectGroupId">${pageScope.projectGroupNotifierSummary.projectGroupId}</ww:param>
            <ww:param name="notifierId">${pageScope.projectGroupNotifierSummary.id}</ww:param>
            <ww:param name="confirmed" value="false"/>
          </ww:url>
        <ww:a href="%{removeUrl}">
            <img src="<ww:url value='/images/delete.gif'/>" alt="Delete" title="Delete" border="0">
        </ww:a>
        </pss:ifAuthorized>
        <pss:elseAuthorized>
          <img src="<ww:url value='/images/delete_disabled.gif'/>" alt="Delete" title="Delete" border="0">
        </pss:elseAuthorized>
      </ec:column>      
     
    </ec:row>
  </ec:table>
  <div class="functnbar3">
    <pss:ifAuthorized permission="continuum-modify-group" resource="${projectGroupName}">
    <ww:form action="addNotifier" method="post">
      <input type="hidden" name="projectGroupId" value="<ww:property value="projectGroupId"/>"/>
      <ww:submit value="%{getText('add')}"/>
    </ww:form>
    </pss:ifAuthorized>
  </div>

  <h3>Project Notifiers</h3>
  <ec:table items="projectNotifierSummaries"
            var="projectNotifierSummary"
            showExports="false"
            showPagination="false"
            showStatusBar="false"
            filterable="false"
            sortable="false">
    <ec:row>
      <ec:column property="type" title="projectView.notifier.type"/>
      <ec:column property="recipient" title="projectView.notifier.recipient"/>
      <ec:column property="events" title="projectView.notifier.events"/>
      <ec:column property="sender" title="projectView.notifier.from"/>
      <ec:column property="state" value="Enabled/Disabled" />
     
    </ec:row>
  </ec:table>  
</ww:i18n>
<%@ taglib uri="/webwork" prefix="ww" %>
<%@ taglib uri="http://www.extremecomponents.org" prefix="ec" %>
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<%@ taglib uri="continuum" prefix="c1" %>
<%@ taglib uri="/plexusSecuritySystem" prefix="pss" %>
<ww:i18n name="localization.Continuum">

  <h3>Project Group Notifiers</h3>
  <ec:table items="projectGroupNotifierSummaries"
            var="projectGroupNotifierSummary"
            showExports="false"
            showPagination="false"
            showStatusBar="false"
            filterable="false"
            sortable="false">
    <ec:row>
      <ec:column property="type" title="projectView.notifier.type"/>
      <ec:column property="recipient" title="projectView.notifier.recipient"/>
      <ec:column property="events" title="projectView.notifier.events"/>
      <ec:column property="sender" title="projectView.notifier.from"/>      
      <ec:column property="editActions" title="Edit">

        <!-- TODO: Fix WW action references for editing notifiers below -->
        <pss:ifAuthorized permission="continuum-modify-group" resource="${projectGroupName}">
          <ww:url id="editUrl" action="addNotifier" method="input" namespace="/">
            <ww:param name="projectGroupId">${pageScope.projectGroupNotifierSummary.projectGroupId}</ww:param>
            <ww:param name="notifierId">${pageScope.projectGroupNotifierSummary.id}</ww:param>
          </ww:url>
          <ww:a href="%{editUrl}">
              <img src="<ww:url value='/images/edit.gif'/>" alt="Edit" title="Edit" border="0">
          </ww:a>
        </pss:ifAuthorized>
        <pss:elseAuthorized>
          <img src="<ww:url value='/images/edit_disabled.gif'/>" alt="Edit" title="Edit" border="0">
        </pss:elseAuthorized>
      </ec:column>    
      <ec:column property="deleteActions" title="Remove">
        <pss:ifAuthorized permission="continuum-modify-group" resource="${projectGroupName}">
          <ww:url id="removeUrl" action="deleteNotifier" namespace="/">
            <ww:param name="projectGroupId">${pageScope.projectGroupNotifierSummary.projectGroupId}</ww:param>
            <ww:param name="notifierId">${pageScope.projectGroupNotifierSummary.id}</ww:param>
            <ww:param name="confirmed" value="false"/>
          </ww:url>
        <ww:a href="%{removeUrl}">
            <img src="<ww:url value='/images/delete.gif'/>" alt="Delete" title="Delete" border="0">
        </ww:a>
        </pss:ifAuthorized>
        <pss:elseAuthorized>
          <img src="<ww:url value='/images/delete_disabled.gif'/>" alt="Delete" title="Delete" border="0">
        </pss:elseAuthorized>
      </ec:column>      
     
    </ec:row>
  </ec:table>
  <div class="functnbar3">
    <pss:ifAuthorized permission="continuum-modify-group" resource="${projectGroupName}">
    <ww:form action="addNotifier" method="post">
      <input type="hidden" name="projectGroupId" value="<ww:property value="projectGroupId"/>"/>
      <ww:submit value="%{getText('add')}"/>
    </ww:form>
    </pss:ifAuthorized>
  </div>

  <h3>Project Notifiers</h3>
  <ec:table items="projectNotifierSummaries"
            var="projectNotifierSummary"
            showExports="false"
            showPagination="false"
            showStatusBar="false"
            filterable="false"
            sortable="false">
    <ec:row>
      <ec:column property="type" title="projectView.notifier.type"/>
      <ec:column property="recipient" title="projectView.notifier.recipient"/>
      <ec:column property="events" title="projectView.notifier.events"/>
      <ec:column property="sender" title="projectView.notifier.from"/>
      <ec:column property="state" value="Enabled/Disabled" />
     
    </ec:row>
  </ec:table>  
</ww:i18n>