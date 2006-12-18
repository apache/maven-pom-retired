/**
 * 
 */
package org.apache.maven.continuum.web.action.component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.model.project.Project;
import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.web.action.ContinuumActionSupport;
import org.apache.maven.continuum.web.model.NotifierSummary;

/**
 * Component Action that prepares and provides Project Group Notifier and 
 * Project Notifier summaries.
 *  
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 * @plexus.component role="com.opensymphony.xwork.Action" role-hint="notifierSummary"
 */
public class NotifierSummaryAction
    extends ContinuumActionSupport
{
    /**
     * Identifier for the {@link ProjectGroup} for which the Notifier summary 
     * needs to be prepared for.
     */
    private int projectGroupId;

    /**
     * Name of the {@link ProjectGroup} for which the Notifier summary needs 
     * to be prepared for.
     */
    private String projectGroupName;

    /**
     * Identifier for the {@link Project} for which the Notifier summary needs 
     * to be prepared for.
     */
    private int projectId;

    /**
     * {@link ProjectGroup} instance to obtain the Notifier summary for.
     */
    private ProjectGroup projectGroup;

    private List projectGroupNotifierSummaries = new ArrayList();

    /**
     * Prepare Notifier summary for a {@link Project}.
     * @return
     */
    public String summarizeForProject()
    {
        getLogger().debug( "Obtaining summary for Project Id: " + projectId );
        return SUCCESS;
    }

    /**
     * Prepare Notifier summary for a {@link ProjectGroup}.
     * @return
     */
    public String summarizeForProjectGroup()
    {
        getLogger().debug( "Obtaining summary for ProjectGroup Id:" + projectGroupId );
        try
        {
            projectGroupNotifierSummaries = gatherGroupNotifierSummaries( projectGroupId );
        }
        catch ( ContinuumException e )
        {
            getLogger().error( "Unable to prepare Notifier summaries for ProjectGroup Id: " + projectGroupId, e );
            return ERROR;
        }
        return SUCCESS;
    }

    /**
     * Prepares and returns {@link ProjectGroup} summaries for the specified project group Id.
     * 
     * @param projectGroupId
     * @return
     * @throws ContinuumException if there was an error fetching the {@link ProjectGroup} for specified Id. 
     */
    private List gatherGroupNotifierSummaries( int projectGroupId )
        throws ContinuumException
    {
        List summaryList = new ArrayList();
        projectGroup = getContinuum().getProjectGroup( projectGroupId );

        for ( Iterator i = projectGroup.getNotifiers().iterator(); i.hasNext(); )
        {
            NotifierSummary ns = generateNotifierSummary( (ProjectNotifier) i.next() );
            summaryList.add( ns );
        }

        return summaryList;
    }

    /**
     * Prepares a {@link NotifierSummary} from a {@link ProjectNotifier} instance.
     * @param notifier
     * @return
     */
    private NotifierSummary generateNotifierSummary( ProjectNotifier notifier )
    {
        NotifierSummary ns = new NotifierSummary();
        ns.setId( notifier.getId() );
        ns.setType( notifier.getType() );
        ns.setProjectGroupId( getProjectGroupId() );

        if ( notifier.isFromProject() )
        {
            ns.setFrom( "PROJECT" );
        }
        else
        {
            ns.setFrom( "USER" );
        }

        // FIXME: Source the recipient 
        ns.setRecipient( "unknown" );
        // XXX: Hack - just for testing :)
        StringBuffer sb = new StringBuffer();
        if ( notifier.isSendOnError() )
            sb.append( "Error" );
        if ( notifier.isSendOnFailure() )
        {
            if ( sb.length() > 0 )
                sb.append( '/' );
            sb.append( "Failure" );
        }
        if ( notifier.isSendOnSuccess() )
        {
            if ( sb.length() > 0 )
                sb.append( '/' );
            sb.append( "Success" );
        }
        if ( notifier.isSendOnWarning() )
        {
            if ( sb.length() > 0 )
                sb.append( '/' );
            sb.append( "Warning" );
        }
        ns.setEvents( sb.toString() );
        
        ns.setEnabled( notifier.isEnabled() );
        return ns;
    }

    // property accessors 

    /**
     * @return the projectGroupId
     */
    public int getProjectGroupId()
    {
        return projectGroupId;
    }

    /**
     * @param projectGroupId the projectGroupId to set
     */
    public void setProjectGroupId( int projectGroupId )
    {
        this.projectGroupId = projectGroupId;
    }

    /**
     * @return the projectGroupName
     */
    public String getProjectGroupName()
    {
        return projectGroupName;
    }

    /**
     * @param projectGroupName the projectGroupName to set
     */
    public void setProjectGroupName( String projectGroupName )
    {
        this.projectGroupName = projectGroupName;
    }

    /**
     * @return the projectId
     */
    public int getProjectId()
    {
        return projectId;
    }

    /**
     * @param projectId the projectId to set
     */
    public void setProjectId( int projectId )
    {
        this.projectId = projectId;
    }

    /**
     * @return the projectGroup
     */
    public ProjectGroup getProjectGroup()
    {
        return projectGroup;
    }

    /**
     * @param projectGroup the projectGroup to set
     */
    public void setProjectGroup( ProjectGroup projectGroup )
    {
        this.projectGroup = projectGroup;
    }

    /**
     * @return the groupBuildDefinitionSummaries
     */
    public List getProjectGroupNotifierSummaries()
    {
        return projectGroupNotifierSummaries;
    }

    /**
     * @param groupBuildDefinitionSummaries the groupBuildDefinitionSummaries to set
     */
    public void setProjectGroupNotifierSummaries( List groupBuildDefinitionSummaries )
    {
        this.projectGroupNotifierSummaries = groupBuildDefinitionSummaries;
    }
}
