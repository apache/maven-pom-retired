<%@ taglib uri="/webwork" prefix="ww" %>
<%@ taglib uri="continuum" prefix="c1" %>
<html>
  <ww:i18n name="localization.Continuum">
    <head>
        <title><ww:text name="notifier.page.add.title"/></title>
    </head>
    <body>
      <div id="axial" class="h3">
      
        <ww:if test="${projectId > 0}">            
            <ww:url id="actionUrl" value="addProjectNotifier!execute" includeParams="none" />
        </ww:if>
        <ww:else>            
            <ww:url id="actionUrl" value="addProjectGroupNotifier!execute" includeParams="none" />
        </ww:else>
       
        <h3><ww:text name="notifier.section.add.title"/></h3>

        <div class="axial">
                
          <ww:form action="%{actionUrl}" method="post">        
            <ww:hidden name="projectId"/>
            <ww:hidden name="projectGroupId"/>
            <table>
              <tbody>
                <ww:select label="%{getText('notifier.type.label')}" name="notifierType"
                           list="#@java.util.LinkedHashMap@{'mail':'Mail', 'irc':'IRC', 'jabber':'Jabber', 'msn':'MSN'}"/>
              </tbody>
            </table>
            <div class="functnbar3">
              <c1:submitcancel value="%{getText('submit')}" cancel="%{getText('cancel')}"/>
            </div>
          </ww:form>
        </div>
      </div>
    </body>
  </ww:i18n>
</html>
