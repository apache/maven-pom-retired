package org.apache.maven.continuum.scheduler;

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
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id$
 */
public class ContinuumSchedulerConstants
{
    /** Checkout before performing a build */
    public static final int SCM_MODE_CHECKOUT = 0;

    /** Update before performing a build */
    public static final int SCM_MODE_UPDATE = 1;

    // ----------------------------------------------------------------------
    // Default Schedule
    // ----------------------------------------------------------------------

    /** Default schedule name */
    public static final String DEFAULT_SCHEDULE_NAME = "Default";

    /** Default schedule description */
    public static final String DEFAULT_SCHEDULE_DESC = "Default Continuum Schedule";

    /** Default scm mode which is to update */
    public static final int DEFAULT_SCHEDULE_SCM_MODE = SCM_MODE_UPDATE;

    /** Every hour on the hour */
    public static final String DEFAULT_CRON_EXPRESSION = "0 0 * * * ?";

    // ----------------------------------------------------------------------
    // Keys for JobDataMap
    // ----------------------------------------------------------------------

    public static final String CONTINUUM = "continuum";

    public static final String SCHEDULE = "schedule";

    public static final String BUILD_SETTINGS = "build-settings";
}
