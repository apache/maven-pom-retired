/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package org.apache.maven.continuum.core.action;

import java.io.File;
import java.util.Map;

import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.scm.queue.CheckOutTask;

import org.codehaus.plexus.taskqueue.TaskQueue;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class AddProjectToCheckOutQueueAction
    extends AbstractContinuumAction
{
    /**
     * @plexus.requirement
     */
    private TaskQueue checkOutQueue;

    public void execute( Map context )
        throws Exception
    {
        ContinuumProject project = getProject( context );

        CheckOutTask checkOutTask = new CheckOutTask( project.getId(),
                                                      new File( project.getWorkingDirectory() ) );

        checkOutQueue.put( checkOutTask );
    }
}
