/**
 * 
 */
package org.apache.maven.continuum.web.action.notifier;

import org.apache.maven.continuum.model.project.ProjectGroup;
import org.apache.maven.continuum.model.project.ProjectNotifier;
import org.apache.maven.continuum.web.action.ContinuumActionSupport;

/**
 * WW action that sets up a new {@link ProjectNotifier} instance for 
 * the specified {@link ProjectGroup}.
 * 
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 * @since 1.1
 * @plexus.component 
 *   role="com.opensymphony.xwork.Action" 
 *   role-hint="addGroupNotifier"
 */
public class AddGroupNotifierAction
    extends ContinuumActionSupport
{

    /**
     * Target {@link ProjectGroup} instance to add the Notifier for.
     */
    private int projectGroupId;

    /**
     * String based type identifier for the {@link ProjectNotifier}.
     */
    private String notifierType;

    /**
     * Default action method executed in case no method is specified 
     * for invocation.
     * @return a String result that determines the control flow.
     */
    public String execute()
    {
        return notifierType + "_" + INPUT;
    }

    public String doDefault()
    {
        return INPUT;
    }

    /**
     * Returns the type identifier for the {@link ProjectNotifier} being 
     * edited as String.
     * 
     * @return notifier type as String.
     */
    public String getNotifierType()
    {
        return notifierType;
    }

    /**
     * Sets the notifier type for the {@link ProjectNotifier} instance 
     * being edited.
     * @param notifierType notifier type to set.
     */
    public void setNotifierType( String notifierType )
    {
        this.notifierType = notifierType;
    }

    /**
     * Returns the current {@link ProjectGroup} Identifier.
     * @return the projectGroupId
     */
    public int getProjectGroupId()
    {
        return projectGroupId;
    }

    /**
     * Sets the Id for the target {@link ProjectGroup}.
     * @param projectGroupId the projectGroupId to set
     */
    public void setProjectGroupId( int projectGroupId )
    {
        this.projectGroupId = projectGroupId;
    }

}
