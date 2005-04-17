package org.codehaus.continuum.web.tool;

import org.codehaus.plexus.formica.web.ContentGenerator;
import org.apache.maven.continuum.project.ContinuumProject;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id: StateContentGenerator.java,v 1.1 2005/04/04 14:05:38 jvanzyl Exp $
 */
public class ContinuumStateContentGenerator
    implements ContentGenerator
{
    public String generate( Object item )
    {
        ContinuumProject p = (ContinuumProject) item;

        int state = p.getState();

        if ( state == 1 )
        {
            return "New";
        }
        else if ( state == 2 )
        {
            return "<img src=\"/continuum/images/icon_success_sml.gif\" alt=\"Success\"/>";
        }
        else if ( state == 3 )
        {
            return "<img src=\"/continuum/images/icon_error_sml.gif\" alt=\"Failed\"/>";
        }
        else if ( state == 4 )
        {
            return "<img src=\"/continuum/images/icon_warning_sml.gif\" alt=\"Error\"/>";
        }
        else if ( state == 5 )
        {
            return "Build Queued";
        }
        else
        {
            return "Building";
        }
    }
}
