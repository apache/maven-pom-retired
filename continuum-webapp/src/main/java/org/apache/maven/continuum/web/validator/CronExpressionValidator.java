package org.apache.maven.continuum.web.validator;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import com.opensymphony.xwork.validator.validators.ValidatorSupport;
import com.opensymphony.xwork.validator.ValidationException;
import com.opensymphony.xwork.validator.ValidatorContext;
import org.quartz.CronTrigger;

import java.text.ParseException;

/**
 *
 * Validator class for the cron expression in the continuum schedules.
 */
public class CronExpressionValidator
    extends ValidatorSupport
{

    public void validate( Object object )
        throws ValidationException
    {
        String second = ( String ) getFieldValue( "second", object);
        String minute = ( String ) getFieldValue( "minute", object );
        String hour = ( String ) getFieldValue( "hour", object );
        String dayOfMonth = ( String ) getFieldValue( "dayOfMonth", object );
        String month = ( String ) getFieldValue( "month", object );
        String dayOfWeek = ( String ) getFieldValue( "dayOfWeek", object );
        String year = ( String ) getFieldValue( "year", object );

        String cronExpression = ( second + " " + minute + " " + hour + " " + dayOfMonth + " " +
            month + " " + dayOfWeek + " " + year ).trim();

        org.codehaus.plexus.scheduler.CronExpressionValidator validator =
            new org.codehaus.plexus.scheduler.CronExpressionValidator();

        ValidatorContext ctxt = getValidatorContext();

        if ( !validator.validate( cronExpression ) )
        {
            ctxt.addActionError( "Invalid cron expression value(s)" );
            return;
        }
    }

}
