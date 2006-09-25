package org.apache.maven.continuum.web.view;

import org.apache.maven.continuum.project.ContinuumProjectState;
import org.apache.maven.continuum.web.model.GroupSummary;
import org.apache.maven.continuum.web.util.StateGenerator;
import org.extremecomponents.table.bean.Column;
import org.extremecomponents.table.cell.DisplayCell;
import org.extremecomponents.table.core.TableModel;

/**
 * 
 * @deprecated use of cells is discouraged due to lack of i18n and design in java code.
 *             Use jsp:include instead.
 * 
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class BuildStatusCell 
    extends DisplayCell
{
    protected String getCellValue( TableModel tableModel, Column column )
    {
        GroupSummary group = (GroupSummary) tableModel.getCurrentRowBean();

        String success = StateGenerator.generate( ContinuumProjectState.OK, tableModel.getContext().getContextPath() );

        String failed = StateGenerator.generate( ContinuumProjectState.FAILED, tableModel.getContext().getContextPath() );
        
        String error = StateGenerator.generate( ContinuumProjectState.ERROR, tableModel.getContext().getContextPath() );
        
        StringBuffer cellContent = new StringBuffer();
        cellContent.append( success );
        cellContent.append( "&nbsp;" );
        cellContent.append( group.getNumSuccesses() );
        cellContent.append( "&nbsp;&nbsp;&nbsp;" );
        cellContent.append( failed );
        cellContent.append( "&nbsp;" );
        cellContent.append( group.getNumFailures() );
        cellContent.append( "&nbsp;&nbsp;&nbsp;" );
        cellContent.append( error );
        cellContent.append( "&nbsp;" );
        cellContent.append( group.getNumErrors() );
        
        return cellContent.toString();
    }
}
