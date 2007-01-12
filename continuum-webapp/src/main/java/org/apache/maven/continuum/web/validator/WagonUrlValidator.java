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

import java.net.URL;
import java.net.MalformedURLException;

/**
 *
 * Validator class for Wagon URLs
 * 
 * @author <a href="mailto:hisidro@exist.com">Henry Isidro</a>
 */
public class WagonUrlValidator
    extends ValidatorSupport
{

    public void validate( Object object )
        throws ValidationException
    {
        String url = ( String ) getFieldValue( "url", object);

        if ( ( url == null ) || ( url.length() == 0 ) )
        {
            return;
        }
        
        if ( url.startsWith( "dav:" ) )
        {
            url = url.substring( 4 );
        }
        
        if ( ( url.startsWith( "scp://" ) ) || ( url.startsWith( "sftp://" ) ) )
        {
            // URL doesn't understand these protocols, hack it
            url = "http://" + url.substring( url.indexOf( "://" ) + 3 ) ;
        }
        
        try
        {
            new URL( url );
        }
        catch ( MalformedURLException m )
        {
            addFieldError( "url", object );
        }
    }
}
