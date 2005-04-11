package org.apache.maven.continuum.web.pipeline.valve;

import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.web.context.ViewContextPopulator;
import org.codehaus.plexus.summit.pipeline.valve.CreateViewContextValve;
import org.codehaus.plexus.summit.rundata.RunData;
import org.codehaus.plexus.summit.view.ViewContext;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id: ContinuumViewContextPopulatorValve.java,v 1.1 2005/04/04 14:05:38 jvanzyl Exp $
 */
public class ContinuumViewContextPopulatorValve
    extends CreateViewContextValve
{
    private ViewContextPopulator viewContextPopulator;

    private Continuum model;

    protected void populateViewContext( RunData data, ViewContext context )
    {
        if ( data.getTarget() != null )
        {
            String view = data.getTarget();

            view = view.substring( 0, view.indexOf( "." ) );

            if ( view != null )
            {

                try
                {
                    // ----------------------------------------------------------------------
                    // We take the parameters from the request so that they can be used to
                    // parameterize the expressions used to extract values out of the
                    // application model.
                    // ----------------------------------------------------------------------

                    Map parameters = new HashMap();

                    for ( Enumeration e = data.getRequest().getParameterNames(); e.hasMoreElements(); )
                    {
                        String key = (String) e.nextElement();

                        parameters.put( key, data.getRequest().getParameter( key ) );
                    }

                    // ----------------------------------------------------------------------
                    // Now that we have the parameters we pass those in along with the
                    // application model to create the set of scalars that will be
                    // placed in the requested view.
                    // ----------------------------------------------------------------------

                    Map scalars = viewContextPopulator.getScalars( view, model, parameters );

                    context.putAll( scalars );
                }
                catch ( Exception e )
                {
                    getLogger().error( "Error inserting scalars into the view context.", e );
                }
            }
        }
    }
}
