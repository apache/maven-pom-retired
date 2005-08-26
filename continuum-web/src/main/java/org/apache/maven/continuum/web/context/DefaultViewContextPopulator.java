package org.apache.maven.continuum.web.context;

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

import ognl.Ognl;
import ognl.OgnlException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.List;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 * @todo collapse this all into the context view populator valve.
 */
public class DefaultViewContextPopulator
    extends AbstractLogEnabled
    implements ViewContextPopulator, Initializable
{
    private List views;

    private Map viewMap;

    public Map getScalars( String viewId, Object model, Map parameters )
    {
        View view = (View) viewMap.get( viewId );

        Map contextScalars = new HashMap();

        if ( view != null )
        {
            for ( Iterator i = view.getScalars().iterator(); i.hasNext(); )
            {
                Scalar scalar = (Scalar) i.next();

                try
                {
                    Object value = Ognl.getValue( scalar.getExpression(), parameters, model );

                    contextScalars.put( scalar.getId(), value );
                }
                catch ( OgnlException e )
                {
                    // ----------------------------------------------------------------------
                    // If there is a problem extracting a value using the expression then
                    // just put the expression in the context for debugging and warn the
                    // driver that something is wrong.
                    // ----------------------------------------------------------------------

                    contextScalars.put( scalar.getId(), scalar.getExpression() );

                    getLogger().warn( "Cannot find a value for the expression " + scalar.getExpression() + "in " + model );
                }
            }
        }

        return contextScalars;
    }

    public void initialize()
    {
        viewMap = new HashMap();

        for ( Iterator i = views.iterator(); i.hasNext(); )
        {
            View view = (View) i.next();

            viewMap.put( view.getId(), view );
        }
    }
}
