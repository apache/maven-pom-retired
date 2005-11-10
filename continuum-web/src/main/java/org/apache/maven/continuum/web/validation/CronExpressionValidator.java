package org.apache.maven.continuum.web.validation;

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

import org.codehaus.plexus.formica.FormicaException;
import org.codehaus.plexus.formica.validation.AbstractValidator;
import org.codehaus.plexus.util.StringUtils;
import org.quartz.CronTrigger;

import java.text.ParseException;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class CronExpressionValidator
    extends AbstractValidator
{
    public boolean validate( String cronExpression )
        throws FormicaException
    {
        try
        {
            String[] cronParams = StringUtils.split( cronExpression );
            if ( cronParams.length != 6 )
            {
                return false;
            }

            CronTrigger cronTrigger = new CronTrigger();

            cronTrigger.setCronExpression( cronExpression );

            if ( cronParams[3].equals( "?" ) || cronParams[5].equals( "?" ) )
            {
                return true;
            }

            return false;
        }
        catch ( ParseException e )
        {
            return false;
        }
    }
}
