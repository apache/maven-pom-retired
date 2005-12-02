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

import org.codehaus.plexus.PlexusTestCase;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class CronExpressionValidatorTest
    extends PlexusTestCase
{
    public void testValidation()
        throws Exception
    {
        CronExpressionValidator validator = new CronExpressionValidator();

        assertTrue( validator.validate( "0 0 * * * ?" ) );

        assertTrue( validator.validate( "0 0 * ? * *" ) );

        assertTrue( validator.validate( "0 0,15,30,45 * * * ?" ) );

        assertTrue( validator.validate( "0 0 12 * * ?" ) );

        assertTrue( validator.validate( "0 15 10 ? * *" ) );

        assertTrue( validator.validate( "0 15 10 * * ?" ) );

        assertTrue( validator.validate( "0 15 10 * * ? *" ) );

        assertTrue( validator.validate( "0 15 10 * * ? 2005" ) );

        assertTrue( validator.validate( "0 * 14 * * ?" ) );

        assertTrue( validator.validate( "0 0/5 14 * * ?" ) );

        assertTrue( validator.validate( "0 0/5 14,18 * * ?" ) );

        assertTrue( validator.validate( "0 0-5 14 * * ?" ) );

        assertTrue( validator.validate( "0 10,44 14 ? 3 WED" ) );

        assertTrue( validator.validate( "0 15 10 ? * MON-FRI" ) );

        assertTrue( validator.validate( "0 15 10 15 * ?" ) );

        assertTrue( validator.validate( "0 15 10 L * ?" ) );

        assertTrue( validator.validate( "0 15 10 ? * 6L" ) );

        assertTrue( validator.validate( "0 15 10 ? * 6L" ) );

        assertTrue( validator.validate( "0 15 10 ? * 6L 2002-2005" ) );

        assertTrue( validator.validate( "0 15 10 ? * 6#3" ) );

        assertFalse( validator.validate( "0 0" ) );

        assertFalse( validator.validate( "0 0 * * * *" ) );

        assertFalse( validator.validate( "a a a a a a" ) );

        assertFalse( validator.validate( "0 0 0 ? 0 A" ) );
    }
}
