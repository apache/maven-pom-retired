package org.apache.maven.continuum.web.pipeline.valve;

import org.apache.maven.continuum.web.tool.FormToolException;
import org.apache.maven.continuum.web.tool.FormicaTool;

import org.codehaus.plexus.formica.Form;
import org.codehaus.plexus.summit.pipeline.valve.CreateViewContextValve;
import org.codehaus.plexus.summit.pipeline.valve.ValveInvocationException;
import org.codehaus.plexus.summit.rundata.RunData;
import org.codehaus.plexus.summit.view.ViewContext;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id: ContinuumViewContextValve.java 170819 2005-05-18 20:34:26Z trygvis $
 */
public class FormicaValve
    extends CreateViewContextValve
{
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
        // ----------------------------------------------------------------------
        // Entity Id
        // ----------------------------------------------------------------------

        String id = data.getParameters().getString( "id" );

        context.put( "id", id );

        // ----------------------------------------------------------------------
        // Form
        // ----------------------------------------------------------------------

        String formId = data.getParameters().getString( "fid" );

        context.put( "fid", formId );

        if ( formId == null )
        {
            formId = defaultFormId;
        }

        Form form;

        try
        {
            form = formicaTool.getForm( formId );

            context.put( "form", form );

            // ----------------------------------------------------------------------
            // We only need the item for the Edit and View
            // ----------------------------------------------------------------------

            String view = data.getTarget();

            if ( view != null && ( view.startsWith( "View" ) || view.startsWith( "Edit" ) ) )
            {
                Object item = formicaTool.getItem( form, id );

                context.put( "item", item );
            }
        }
        catch ( FormToolException e )
        {
            throw new ValveInvocationException( "Error populating context: ", e );
        }
    }
}
