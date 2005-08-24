package org.apache.maven.continuum.web.action;

import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.project.builder.ContinuumProjectBuildingResult;
import org.codehaus.plexus.formica.Form;
import org.codehaus.plexus.formica.action.AbstractEntityAction;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;

import java.net.URL;
import java.util.Map;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
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

        String m2PomUrl = (String) parameters.get( "m2PomUrl" );

        String m2PomFile = (String) parameters.get( "m2PomFile" );

        String m2Pom = null;

        if ( !StringUtils.isEmpty( m2PomUrl ) )
        {
            m2Pom = m2PomUrl;
        }
        else
        {
            URL url = new URL( m2PomFile );

            String content = IOUtil.toString( url.openStream() );

            if ( !StringUtils.isEmpty( content ) )
            {
                m2Pom = m2PomFile;
            }
        }

        if ( !StringUtils.isEmpty( m2Pom ) )
        {
            ContinuumProjectBuildingResult result = continuum.addMavenTwoProject( m2Pom );

            if ( result.getWarnings().size() > 0 )
            {
                setResultMessages( result.getWarnings(), parameters );
            }
        }
    }
}
