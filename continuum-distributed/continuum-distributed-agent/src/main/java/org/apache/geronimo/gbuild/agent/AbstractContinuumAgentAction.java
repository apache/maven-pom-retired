/**
 *
 * Copyright 2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.gbuild.agent;

import org.apache.maven.continuum.core.action.AbstractContinuumAction;
import org.apache.maven.continuum.store.ContinuumStore;

import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
public abstract class AbstractContinuumAgentAction extends AbstractContinuumAction {

    // ----------------------------------------------------------------------
    // Keys for the values that can be in the context
    // ----------------------------------------------------------------------

    public static final String KEY_PROJECT = "project";

    public static final String KEY_BUILD_DEFINITION = "build-definition";

    public static final String KEY_STORE = "store";

    public static final String KEY_PROJECT_ID = "projectId";

    public static final String KEY_BUILD_DEFINITION_ID = "buildDefinitionId";

    public static final String KEY_TRIGGER = "trigger";

    public static final String KEY_HOST_NAME = "host-name";

    public static final String KEY_HOST_ADDRESS = "host-address";

    public static final String KEY_CONTRIBUTOR = "contributor";

    public static final String KEY_ADMIN_ADDRESS = "admin-address";

    public static final String KEY_OS_VERSION = "os.version";

    public static final String KEY_OS_NAME = "os.name";

    public static final String KEY_JAVA_VERSION = "java.version";

    public static final String KEY_JAVA_VENDOR = "java.vendor";

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public static int getTrigger(Map context) {
        return getInteger(context, KEY_TRIGGER);
    }

    public static int getBuildDefinitionId(Map context) {
        return getInteger(context, KEY_BUILD_DEFINITION_ID);
    }

    public static int getProjectId(Map context) {
        return getInteger(context, KEY_PROJECT_ID);
    }

    public static ContinuumStore getContinuumStore(Map context) {
        return (ContinuumStore) getObject(context, KEY_STORE);
    }

    protected static String getString(Map context, String key) {
        return (String) getObject(context, key);
    }

    protected static String getString(Map context, String key, String defaultValue) {
        return (String) getObject(context, key, defaultValue);
    }

    public static boolean getBoolean(Map context, String key) {
        return ((Boolean) getObject(context, key)).booleanValue();
    }

    protected static int getInteger(Map context, String key) {
        return ((Integer) getObject(context, key, null)).intValue();
    }

    protected static Object getObject(Map context, String key) {
        if (!context.containsKey(key)) {
            throw new RuntimeException("Missing key '" + key + "'.");
        }

        Object value = context.get(key);

        if (value == null) {
            throw new RuntimeException("Missing value for key '" + key + "'.");
        }

        return value;
    }

    protected static Object getObject(Map context, String key, Object defaultValue) {
        Object value = context.get(key);

        if (value == null) {
            return defaultValue;
        }

        return value;
    }


}
