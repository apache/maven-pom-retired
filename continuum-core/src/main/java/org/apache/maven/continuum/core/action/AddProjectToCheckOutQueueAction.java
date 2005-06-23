/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package org.apache.maven.continuum.core.action;

import org.apache.maven.continuum.project.ContinuumProject;
import org.apache.maven.continuum.scm.queue.CheckOutTask;

import java.io.File;
import java.util.Map;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class AddProjectToCheckOutQueueAction
    extends AbstractContinuumAction
{
    public void execute( Map context )
        throws Exception
    {
        ContinuumProject project = getProject( context );

        CheckOutTask checkOutTask = new CheckOutTask( project.getId(), new File( project.getWorkingDirectory() ) );

        getCheckOutQueue().put( checkOutTask );
    }
}
