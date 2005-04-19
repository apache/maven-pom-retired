package org.codehaus.continuum.web.action;

import org.codehaus.plexus.summit.rundata.RunData;
import org.codehaus.plexus.action.AbstractAction;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;

import java.util.Map;
import java.util.List;
import java.util.Iterator;
import java.util.HashMap;

import ognl.Ognl;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id: Login.java,v 1.1 2005/04/01 00:11:34 jvanzyl Exp $
 */
public class CallApplicationModel
    extends AbstractAction
    implements Initializable
{
    private static String CID = "cid";

    private String applicationRole;

    private List calls;

    private Map callMap;

    public void execute( Map parameters )
        throws Exception
    {
        Object application = lookup( applicationRole );

        System.out.println( "application = " + application );

        String cid = (String) parameters.get( CID );

        Call call = (Call) callMap.get( cid );

        String expression = call.getExpression();

        System.out.println( "expression = " + expression );

        Ognl.getValue( expression, parameters, application );
    }

    // ----------------------------------------------------------------------
    // Lifecylce Management
    // ----------------------------------------------------------------------

    public void initialize()
        throws Exception
    {
        callMap = new HashMap();

        for ( Iterator i = calls.iterator(); i.hasNext(); )
        {
            Call call = (Call) i.next();

            callMap.put( call.getCid(), call );
        }
    }
}
