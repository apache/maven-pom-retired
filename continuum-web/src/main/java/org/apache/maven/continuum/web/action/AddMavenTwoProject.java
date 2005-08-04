package org.apache.maven.continuum.web.action;

import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.codehaus.plexus.formica.Form;
import org.codehaus.plexus.formica.action.AbstractEntityAction;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;

import java.util.Map;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id$
 */
public class AddMavenTwoProject
    extends AbstractEntityAction
    implements Contextualizable
{
    protected void uponSuccessfulValidation( Form form, String entityId, Map parameters )
        throws Exception
    {
        Continuum continuum = (Continuum) container.lookup( Continuum.ROLE );

        ContinuumProjectBuildingResult result = continuum.addMavenTwoProject( (String) parameters.get( "m2PomUrl" ) );

        if ( result.getWarnings().size() > 0 )
        {
            setResultMessages( result.getWarnings(), parameters );
        }
    }
}
