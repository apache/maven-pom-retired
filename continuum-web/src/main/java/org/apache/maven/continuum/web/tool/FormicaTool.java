package org.apache.maven.continuum.web.tool;

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
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.formica.Element;
import org.codehaus.plexus.formica.Form;
import org.codehaus.plexus.formica.FormManager;
import org.codehaus.plexus.formica.FormNotFoundException;
import org.codehaus.plexus.formica.web.ContentGenerator;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.summit.rundata.RunData;
import org.codehaus.plexus.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class FormicaTool
    extends AbstractLogEnabled
    implements Contextualizable
{
    private FormManager formManager;

    private PlexusContainer container;

    // ----------------------------------------------------------------------
    // Form
    // ----------------------------------------------------------------------

    public Form getForm( String fid )
        throws FormToolException
    {
        try
        {
            return formManager.getForm( fid );
        }
        catch ( FormNotFoundException e )
        {
            throw new FormToolException( "Cannot find form with id = " + fid, e );
        }
    }


    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public String getItem( Form form, Element element, String id )
        throws FormToolException
    {
        if ( id == null )
        {
            return null;
        }

        return getItem( form, element, getItem( form, id ) );
    }

    public String getItem( Form form, Element element, Object item )
        throws FormToolException
    {
        try
        {
            String text;

            if ( element.getContentGenerator() != null )
            {
                ContentGenerator cg = (ContentGenerator) container.lookup( ContentGenerator.ROLE, element.getContentGenerator() );

                text = cg.generate( item );
            }
            else
            {
                Object value =  Ognl.getValue( element.getExpression(), item );

                text = value.toString();

            }

            return text;
        }
        catch( ComponentLookupException e )
        {
            throw new FormToolException( "Content generator with id = " + element.getContentGenerator() + " does not exist", e );
        }
        catch ( OgnlException e )
        {
            throw new FormToolException( "Error extracting value from " + item + " using the expression " + element.getExpression(), e );
        }
    }

    /**
     * For a given element in a form, get the data for that form whether it be from a request
     * parameter or by applying an expression to the item in question.
     *
     * @param form Formica form object.
     * @param element Form element.
     * @param item The object from which the data will be extracted using an expression.
     * @param runData The summit runData.
     * @return
     * @throws FormToolException
     */
    public String getElementData( Form form, Element element, Object item, RunData runData )
        throws FormToolException
    {
        // ----------------------------------------------------------------------
        // First try to get the form data from the request parameters
        // ----------------------------------------------------------------------

        Object data = runData.getParameters().getString( element.getId() );

        if ( data != null )
        {
            return data.toString();
        }

        if ( item != null )
        {
            try
            {
                // ----------------------------------------------------------------------
                // Here we need to decompose data into constituent form element
                // data if necessary.
                // ----------------------------------------------------------------------

                data = Ognl.getValue( element.getExpression(), item );

                if ( data != null )
                {
                    return data.toString();
                }
            }
            catch ( OgnlException e )
            {
                throw new FormToolException( "Error evaluating expression: ", e );
            }
        }

        return "";
    }

    // ----------------------------------------------------------------------
    // View
    // ----------------------------------------------------------------------

    public Object getItem( Form form, String id )
        throws FormToolException
    {
        Object component;

        try
        {
            component = container.lookup( assertNotEmpty( form, form.getSourceRole(), "source role" ) );
        }
        catch ( ComponentLookupException e )
        {
            throw new FormToolException( "Cannot lookup source component.", e );
        }

        Map map = new HashMap();

        map.put( "id", id );

        String expr = assertNotEmpty( form, form.getLookupExpression(), "lookup expression" );

        return getValue( expr, map, component );
    }

    // ----------------------------------------------------------------------
    // Utilities
    // ----------------------------------------------------------------------

    private String assertNotEmpty( Form form, String value, String field )
        throws FormToolException
    {
        if ( StringUtils.isEmpty( value ) )
        {
            throw new FormToolException( "Missing " + field + " from form '" + form.getId() + "'." );
        }

        return value;
    }

    private Object getValue( String expr, Map map, Object o )
        throws FormToolException
    {
        Object data;

        try
        {
            data = Ognl.getValue( expr, map, o );
        }
        catch ( Throwable e )
        {
            getLogger().error( "Error while evaluation OGNL expression '" + expr + "'.", e );

            throw new FormToolException( "Error while evaluation OGNL expression '" + expr + "'.", e );
        }

        return data;
    }

    // ----------------------------------------------------------------------
    // Lifecylce Management
    // ----------------------------------------------------------------------

    public void contextualize( Context context )
        throws ContextException
    {
        container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
    }
}
