package org.apache.maven.continuum.store;

import org.apache.maven.continuum.AbstractContinuumTest;
import org.apache.maven.continuum.initialization.ContinuumInitializer;

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

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id:$
 */
public class ContinuumTypicalUsageTest
    extends AbstractContinuumTest
{
    // ----------------------------------------------------------------------
    // 1. Create the default project group
    // 2. Create the default build settings
    // ----------------------------------------------------------------------

    public void testContinuumTypicalUsage()
        throws Exception
    {
        // ----------------------------------------------------------------------
        // Simulate the initial running of the system which will create the
        // default project group and the default build settings.
        // ----------------------------------------------------------------------

        ContinuumInitializer initializer = (ContinuumInitializer) lookup( ContinuumInitializer.ROLE );

        initializer.initialize();
    }
}
