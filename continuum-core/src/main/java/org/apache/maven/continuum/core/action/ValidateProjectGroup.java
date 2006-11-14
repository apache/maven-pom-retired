/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package org.apache.maven.continuum.core.action;

import org.apache.maven.continuum.model.project.ProjectGroup;

import java.util.Map;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 *
 * @plexus.component
 *   role="org.codehaus.plexus.action.Action"
 *   role-hint="validate-project-group"
 */
public class ValidateProjectGroup
    extends AbstractValidationContinuumAction
{
    public void execute( Map context )
        throws Exception
    {
        ProjectGroup projectGroup = getUnvalidatedProjectGroup( context );

        // TODO: assert that the name is unique

        assertStringNotEmpty( projectGroup.getName(), "name" );

        assertStringNotEmpty( projectGroup.getGroupId(), "group id" );
    }
}
