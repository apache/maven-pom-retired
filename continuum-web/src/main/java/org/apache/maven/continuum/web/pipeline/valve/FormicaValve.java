package org.apache.maven.continuum.web.pipeline.valve;

import org.apache.maven.continuum.web.tool.FormToolException;
import org.apache.maven.continuum.web.tool.FormicaTool;

import org.codehaus.plexus.formica.Form;
import org.codehaus.plexus.formica.action.FormInfo;
import org.codehaus.plexus.summit.pipeline.valve.CreateViewContextValve;
import org.codehaus.plexus.summit.pipeline.valve.ValveInvocationException;
import org.codehaus.plexus.summit.rundata.RunData;
import org.codehaus.plexus.summit.view.ViewContext;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class FormicaValve
    extends CreateViewContextValve
{
    public static final String FORMICA_REDIRECT = "formicaRedirect";

    // ----------------------------------------------------------------------
    // Requirements
    // ----------------------------------------------------------------------

    /**
     * @plexus.requirement
     */
    private FormicaTool formicaTool;

    // ----------------------------------------------------------------------
    // Configuration
    // ----------------------------------------------------------------------

    /**
     * @plexus.configuration
     */
    private String defaultFormId;

    protected void populateViewContext( RunData data, ViewContext context )
        throws ValveInvocationException
    {
        FormInfo formInfo = (FormInfo) data.getMap().get( "formInfo" );

        String formId;

        // ----------------------------------------------------------------------
        // We'll use a formInfo object if we have one, otherwise we will look
        // in the request for form info.
        // ----------------------------------------------------------------------

        if ( formInfo != null )
        {
            formId = formInfo.getFid();
        }
        else if ( data.getParameters().getString( FORMICA_REDIRECT ) != null )
        {
            String[] s = StringUtils.split( data.getParameters().getString( FORMICA_REDIRECT ), ":" );

            formId = s[0];

            data.setTarget( s[1] );
        }
        else
        {
            formId = data.getParameters().getString( "fid" );
        }

        // ----------------------------------------------------------------------
        // If a formId can't be found in either place then we're not dealing with
        // a form so we can safely return.
        // ----------------------------------------------------------------------

        if ( formId == null )
        {
            return;
        }

        // ----------------------------------------------------------------------
        // Entity Id
        // ----------------------------------------------------------------------

        String id = data.getParameters().getString( "id" );

        context.put( "id", id );

        // ----------------------------------------------------------------------
        // Form
        // ----------------------------------------------------------------------

        context.put( "fid", formId );

        Form form;

        try
        {
            form = formicaTool.getForm( formId );

            context.put( "form", form );

            // ----------------------------------------------------------------------
            // We only need the item for the Edit and View
            // ----------------------------------------------------------------------

            String view = data.getTarget();

            if ( view != null && ( view.indexOf( "View" ) >= 0 || view.indexOf( "Edit" ) >= 0 ) )
            {
                Object item = formicaTool.getItem( form, id );

                context.put( "item", item );
            }
        }
        catch ( FormToolException e )
        {
            throw new ValveInvocationException( "Error populating context.", e );
        }
    }
}
