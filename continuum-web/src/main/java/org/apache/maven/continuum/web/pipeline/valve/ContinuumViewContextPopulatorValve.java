package org.apache.maven.continuum.web.pipeline.valve;

/*
 * Copyright 2004-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.web.context.ViewContextPopulator;
import org.codehaus.plexus.summit.pipeline.valve.CreateViewContextValve;
import org.codehaus.plexus.summit.rundata.RunData;
import org.codehaus.plexus.summit.view.ViewContext;

import java.util.HashMap;
import java.util.Iterator;
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
                    // application model. We use the request parameters as the have been
                    // parsed from the ReqestParameters class.
                    // ----------------------------------------------------------------------

                    Map parameters = new HashMap();

                    for ( Iterator i  = data.getParameters().getParametersMap().keySet().iterator(); i.hasNext(); )
                    {
                        String key = (String) i.next();

                        parameters.put( key, data.getParameters().getString( key ) );
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
