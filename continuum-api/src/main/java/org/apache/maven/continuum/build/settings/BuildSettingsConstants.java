package org.apache.maven.continuum.build.settings;

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

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id$
 */
public class BuildSettingsConstants
{
    // ----------------------------------------------------------------------
    // Notification
    // ----------------------------------------------------------------------

    public static final int NOTIFICATION_STRATEGY_ALWAYS = 0;

    public static final int NOTIFICATION_STRATEGY_NEVER = 1;

    public static final int NOTIFICATION_STRATEGY_ON_FAILURE = 2;

    // ----------------------------------------------------------------------
    // Labelling
    // ----------------------------------------------------------------------

    public static final int LABELLING_STRATEGY_ALWAYS = 0;

    public static final int LABELLING_STRATEGY_NEVER = 0;

    public static final int LABELLING_STRATEGY_ON_SUCCESS = 0;

    // ----------------------------------------------------------------------
    // Scm mode
    // ----------------------------------------------------------------------

    public static final int SCM_MODE_CLEAN_CHECKOUT = 0;

    public static final int SCM_MODE_UPDATE = 1;

    // ----------------------------------------------------------------------
    // Cron scheduling
    // ----------------------------------------------------------------------
}
