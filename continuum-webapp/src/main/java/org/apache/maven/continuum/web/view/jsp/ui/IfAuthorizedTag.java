package org.apache.maven.continuum.web.view.jsp.ui;

import com.opensymphony.xwork.ActionContext;
import org.apache.maven.continuum.Continuum;
import org.apache.maven.continuum.ContinuumException;
import org.apache.maven.continuum.model.system.ContinuumUser;
import org.apache.maven.continuum.security.ContinuumSecurityException;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.jstl.core.ConditionalTagSupport;
/*
 * Copyright 2005 The Apache Software Foundation.
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

/**
 * IfAuthorizedTag:
 *
 * @author Jesse McConnell <jmcconnell@apache.org>
 * @version $Id:$
 */
public class IfAuthorizedTag
    extends ConditionalTagSupport

{
    private String permission;


    public void setPermission( String permission )
    {
        this.permission = permission;
    }

    protected boolean condition()
        throws JspTagException
    {
        ActionContext context = ActionContext.getContext();

        try
        {
            Continuum continuum = (Continuum) context.getSession().get( "continuum" );

            if ( continuum != null )
            {
                ContinuumUser user = continuum.getUser( ((Integer)context.getSession().get( "userId" )).intValue() );

                return continuum.getSecurity().isAuthorized( user, permission );
            }
            else
            {
                throw new JspTagException("continuum object is null!");
            }
        }
        catch ( ContinuumException e )
        {
            throw new JspTagException( "continuum exception", e );
        }
        catch ( ContinuumSecurityException e )
        {
            throw new JspTagException( "security exception", e );
        }
    }



}
